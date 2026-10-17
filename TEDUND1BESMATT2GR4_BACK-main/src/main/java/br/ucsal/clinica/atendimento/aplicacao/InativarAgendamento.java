package br.ucsal.clinica.atendimento.aplicacao;

import br.ucsal.clinica.atendimento.dominio.Agendamento;
import br.ucsal.clinica.atendimento.porta.AgendamentoRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;

/** Caso de uso InativarAgendamento (RF054, RN030): por restricao ao atendimento, mantendo o historico. */
public class InativarAgendamento {

    private final AgendamentoRepositoryPort agendamentos;

    public InativarAgendamento(AgendamentoRepositoryPort agendamentos) {
        this.agendamentos = agendamentos;
    }

    public Agendamento executar(Long id, String motivo) {
        Agendamento agendamento = agendamentos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento", id));
        agendamento.inativar(motivo);
        return agendamentos.salvar(agendamento);
    }
}
