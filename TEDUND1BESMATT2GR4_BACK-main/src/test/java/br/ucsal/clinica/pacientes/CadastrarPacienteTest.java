package br.ucsal.clinica.pacientes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.ucsal.clinica.administracao.dominio.Colaborador;
import br.ucsal.clinica.administracao.dominio.Convenio;
import br.ucsal.clinica.administracao.dominio.Especialidade;
import br.ucsal.clinica.administracao.dominio.Funcao;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.compartilhado.dominio.Status;
import br.ucsal.clinica.pacientes.aplicacao.CadastrarPaciente;
import br.ucsal.clinica.pacientes.aplicacao.InativarPaciente;
import br.ucsal.clinica.pacientes.dominio.Paciente;
import br.ucsal.clinica.pacientes.dominio.ProntuarioEletronico;
import br.ucsal.clinica.testes.Fakes;

/** RN007 a RN009 / DA012: paciente e prontuario nascem juntos; RF052 convenio com carteirinha. */
class CadastrarPacienteTest {

    private Fakes.Pacientes pacientes;
    private Fakes.Prontuarios prontuarios;
    private CadastrarPaciente cadastrar;
    private Convenio convenio;

    @BeforeEach
    void preparar() {
        pacientes = new Fakes.Pacientes();
        prontuarios = new Fakes.Prontuarios();
        Fakes.Colaboradores colaboradores = new Fakes.Colaboradores();
        Colaborador medico = new Colaborador();
        medico.setNomeCompleto("Dra. Ana");
        medico.setCpf("11111111111");
        medico.setFuncao(Funcao.MEDICO);
        medico.setHoraInicio(LocalTime.of(8, 0));
        colaboradores.salvar(medico);
        Fakes.Especialidades especialidades = new Fakes.Especialidades();
        especialidades.salvar(new Especialidade(null, "Clínica Geral", Status.ATIVO));
        Fakes.Convenios convenios = new Fakes.Convenios();
        convenio = new Convenio();
        convenio.setNomeFantasia("Saúde Bahia");
        convenios.salvar(convenio);
        cadastrar = new CadastrarPaciente(pacientes, prontuarios, colaboradores, especialidades, convenios);
    }

    private Paciente novo() {
        Paciente p = new Paciente();
        p.setNomeCompleto("Maria das Graças");
        p.setDataNascimento(LocalDate.of(1965, 3, 12));
        p.setMedico(Referencia.de(1L));
        p.setEspecialidade(Referencia.de(1L));
        return p;
    }

    @Test
    void cadastroGeraOProntuarioDoPaciente() {
        Paciente salvo = cadastrar.executar(novo());
        ProntuarioEletronico pr = prontuarios.buscarPorPaciente(salvo.getId()).orElseThrow();
        assertEquals("PR-000001", pr.getNumero());
        assertEquals(pr.getId(), salvo.getProntuario().id());
        assertEquals(1, prontuarios.todos().size());
    }

    @Test
    void convenioExigeCarteirinha() {
        Paciente p = novo();
        p.setConvenio(Referencia.de(convenio.getId()));
        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> cadastrar.executar(p));
        assertTrue(erro.getMessage().contains("carteirinha"));
        p.setNumeroCarteirinha("SB-1");
        assertEquals("Saúde Bahia", cadastrar.executar(p).getConvenio().nome());
    }

    @Test
    void inativarPacienteInativaOProntuario() {
        Paciente salvo = cadastrar.executar(novo());
        new InativarPaciente(pacientes, prontuarios).executar(salvo.getId(), "Restrição de atendimento");
        assertEquals(Status.INATIVO, prontuarios.buscarPorPaciente(salvo.getId()).orElseThrow().getStatus());
    }
}
