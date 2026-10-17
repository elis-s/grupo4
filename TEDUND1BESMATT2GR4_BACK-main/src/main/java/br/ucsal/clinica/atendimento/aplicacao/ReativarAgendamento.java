package br.ucsal.clinica.atendimento.aplicacao;

import br.ucsal.clinica.atendimento.dominio.Agendamento;
import br.ucsal.clinica.atendimento.porta.AgendamentoRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;

/** Reativa um agendamento, se o horario ainda estiver livre. */
public class ReativarAgendamento {

    private final AgendamentoRepositoryPort agendamentos;

    public ReativarAgendamento(AgendamentoRepositoryPort agendamentos) {
        this.agendamentos = agendamentos;
    }

    public Agendamento executar(Long id) {
        Agendamento agendamento = agendamentos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento", id));
        AgendarConsulta.garantirHorarioLivre(agendamentos, agendamento, id);
        agendamento.reativar();
        return agendamentos.salvar(agendamento);
    }
}
