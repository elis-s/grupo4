package br.ucsal.clinica.atendimento.aplicacao;

import java.time.LocalDate;

import br.ucsal.clinica.administracao.dominio.Colaborador;
import br.ucsal.clinica.administracao.porta.ColaboradorRepositoryPort;
import br.ucsal.clinica.atendimento.dominio.Agendamento;
import br.ucsal.clinica.atendimento.dominio.SituacaoAgendamento;
import br.ucsal.clinica.atendimento.porta.AgendamentoRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.pacientes.dominio.Paciente;
import br.ucsal.clinica.pacientes.porta.PacienteRepositoryPort;

/**
 * Caso de uso AgendarConsulta (RF025, RN010): o agendamento nasce AGENDADO. A agenda do
 * medico e travada (bloqueio pessimista pontual) para evitar dois agendamentos no mesmo
 * horario feitos ao mesmo tempo (secao 3.2.4).
 */
public class AgendarConsulta {

    private final AgendamentoRepositoryPort agendamentos;
    private final ColaboradorRepositoryPort colaboradores;
    private final PacienteRepositoryPort pacientes;

    public AgendarConsulta(AgendamentoRepositoryPort agendamentos, ColaboradorRepositoryPort colaboradores,
            PacienteRepositoryPort pacientes) {
        this.agendamentos = agendamentos;
        this.colaboradores = colaboradores;
        this.pacientes = pacientes;
    }

    public Agendamento executar(Agendamento novo) {
        preparar(novo, colaboradores, pacientes);
        if (novo.getDataPrevista().isBefore(LocalDate.now())) {
            throw new RegraNegocioException("Não é possível agendar uma consulta para uma data passada.");
        }
        novo.setSituacao(SituacaoAgendamento.AGENDADO);
        garantirHorarioLivre(agendamentos, novo, null);
        return agendamentos.salvar(novo);
    }

    /** Medico ativo (funcao MEDICO) e paciente ativo. */
    static void preparar(Agendamento agendamento, ColaboradorRepositoryPort colaboradores,
            PacienteRepositoryPort pacientes) {
        agendamento.validar();
        Long medicoId = agendamento.getMedico().id();
        Colaborador medico = colaboradores.buscarPorId(medicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador", medicoId));
        if (!medico.isMedico() || !medico.isAtivo()) {
            throw new RegraNegocioException(medico.getNomeCompleto() + " não é um médico ativo.");
        }
        Long pacienteId = agendamento.getPaciente().id();
        Paciente paciente = pacientes.buscarPorId(pacienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", pacienteId));
        paciente.garantirAtivoParaAtendimento();
        agendamento.setMedico(new Referencia(medico.getId(), medico.getNomeCompleto()));
        agendamento.setPaciente(new Referencia(paciente.getId(), paciente.getNomeCompleto()));
    }

    /** Trava a agenda do medico e confere se o horario continua livre. */
    static void garantirHorarioLivre(AgendamentoRepositoryPort agendamentos, Agendamento agendamento, Long ignorarId) {
        Long medicoId = agendamento.getMedico().id();
        agendamentos.bloquearAgendaDoMedico(medicoId);
        if (agendamentos.horarioOcupado(medicoId, agendamento.getDataPrevista(), agendamento.getHoraPrevista(),
                ignorarId)) {
            throw new RegraNegocioException("O médico já tem uma consulta agendada neste dia e horário.");
        }
    }
}
