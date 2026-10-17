package br.ucsal.clinica.atendimento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.ucsal.clinica.atendimento.aplicacao.FinalizarAtendimento;
import br.ucsal.clinica.atendimento.aplicacao.IniciarAtendimentoClinico;
import br.ucsal.clinica.atendimento.aplicacao.RealizarTriagemPaciente;
import br.ucsal.clinica.atendimento.aplicacao.RegistrarEvolucaoClinica;
import br.ucsal.clinica.atendimento.dominio.Agendamento;
import br.ucsal.clinica.atendimento.dominio.AtendimentoClinico;
import br.ucsal.clinica.atendimento.dominio.EtapaAtendimento;
import br.ucsal.clinica.atendimento.dominio.SituacaoAgendamento;
import br.ucsal.clinica.atendimento.dominio.TipoAtendimento;
import br.ucsal.clinica.autenticacao.dominio.NomePerfil;
import br.ucsal.clinica.compartilhado.dominio.AcessoNegadoException;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.pacientes.dominio.Paciente;
import br.ucsal.clinica.pacientes.dominio.ProntuarioEletronico;
import br.ucsal.clinica.testes.Fakes;

/** Maquina de estados do atendimento (secao 3.2.4), RN010, RN027, RN028 e RN029. */
class AtendimentoClinicoTest {

    private Fakes.Atendimentos atendimentos;
    private Fakes.Agendamentos agendamentos;
    private Fakes.Pacientes pacientes;
    private Fakes.Prontuarios prontuarios;
    private Fakes.Contexto medica;
    private Paciente paciente;
    private Agendamento agendamento;

    @BeforeEach
    void preparar() {
        atendimentos = new Fakes.Atendimentos();
        agendamentos = new Fakes.Agendamentos();
        pacientes = new Fakes.Pacientes();
        prontuarios = new Fakes.Prontuarios();
        medica = Fakes.Contexto.de(2L, "ana.souza", 3L, "MEDICO", NomePerfil.COLABORADOR);

        paciente = new Paciente();
        paciente.setNomeCompleto("Maria");
        pacientes.salvar(paciente);
        prontuarios.salvar(ProntuarioEletronico.paraPaciente(paciente.getId()));

        agendamento = new Agendamento();
        agendamento.setMedico(new Referencia(3L, "Dra. Ana"));
        agendamento.setPaciente(new Referencia(paciente.getId(), "Maria"));
        agendamento.setDataPrevista(LocalDate.now());
        agendamento.setHoraPrevista(LocalTime.of(9, 0));
        agendamentos.salvar(agendamento);
    }

    private AtendimentoClinico triagem() {
        return new RealizarTriagemPaciente(atendimentos, agendamentos, pacientes, prontuarios, medica)
                .executar(new RealizarTriagemPaciente.Comando(paciente.getId(), agendamento.getId(),
                        TipoAtendimento.CONSULTA, "Febre e dor de cabeça"));
    }

    @Test
    void fluxoCompletoFinalizaOAgendamento() {
        AtendimentoClinico a = triagem();
        assertEquals(EtapaAtendimento.EM_TRIAGEM, a.getEtapa());
        assertEquals(3L, a.getColaborador().id()); // RN028: veio do login

        new IniciarAtendimentoClinico(atendimentos, medica).executar(a.getId());
        new RegistrarEvolucaoClinica(atendimentos, medica).executar(a.getId(),
                new RegistrarEvolucaoClinica.Comando("Febre", "Síndrome gripal", "Repouso", null));
        AtendimentoClinico finalizado = new FinalizarAtendimento(atendimentos, agendamentos, medica)
                .executar(a.getId(), new FinalizarAtendimento.Comando(null, null, false));

        assertEquals(EtapaAtendimento.FINALIZADO, finalizado.getEtapa());
        assertNotNull(finalizado.getDataHoraEncerramento());
        assertEquals(SituacaoAgendamento.FINALIZADO, agendamentos.buscarPorId(agendamento.getId()).orElseThrow()
                .getSituacao()); // RN010
    }

    @Test
    void naoFinalizaSemPassarPeloAtendimentoNemSemDiagnostico() {
        AtendimentoClinico a = triagem();
        FinalizarAtendimento finalizar = new FinalizarAtendimento(atendimentos, agendamentos, medica);
        assertThrows(RegraNegocioException.class,
                () -> finalizar.executar(a.getId(), new FinalizarAtendimento.Comando("Gripe", null, false)));

        new IniciarAtendimentoClinico(atendimentos, medica).executar(a.getId());
        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> finalizar.executar(a.getId(), new FinalizarAtendimento.Comando(null, null, false)));
        assertTrue(erro.getMessage().contains("diagnóstico"));
    }

    @Test
    void atendimentoFinalizadoNaoMudaMais() {
        AtendimentoClinico a = triagem();
        a.iniciarAtendimento();
        a.finalizar("Gripe", "Repouso", false);
        assertThrows(RegraNegocioException.class, () -> a.registrarEvolucao("x", "y", "z"));
    }

    @Test
    void somenteOResponsavelAlteraOAtendimento() {
        AtendimentoClinico a = triagem();
        Fakes.Contexto outroMedico = Fakes.Contexto.de(9L, "marcos.lima", 4L, "MEDICO", NomePerfil.COLABORADOR);
        assertThrows(AcessoNegadoException.class,
                () -> new IniciarAtendimentoClinico(atendimentos, outroMedico).executar(a.getId()));
    }

    @Test
    void prontuarioInativoNaoRecebeAtendimento() {
        prontuarios.buscarPorPaciente(paciente.getId()).orElseThrow().inativar("Restrição judicial"); // RN029
        assertThrows(RegraNegocioException.class, this::triagem);
    }

    @Test
    void enfermeiroNaoRegistraAtendimentoClinico() {
        Fakes.Contexto enfermeira = Fakes.Contexto.de(5L, "juliana", 6L, "ENFERMEIRO", NomePerfil.COLABORADOR);
        assertThrows(AcessoNegadoException.class, () -> new RealizarTriagemPaciente(atendimentos, agendamentos,
                pacientes, prontuarios, enfermeira).executar(new RealizarTriagemPaciente.Comando(paciente.getId(),
                        null, TipoAtendimento.URGENCIA, "Dor")));
    }
}
