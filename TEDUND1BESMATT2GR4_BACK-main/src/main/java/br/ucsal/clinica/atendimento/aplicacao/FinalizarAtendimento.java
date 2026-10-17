package br.ucsal.clinica.atendimento.aplicacao;

import br.ucsal.clinica.atendimento.dominio.AtendimentoClinico;
import br.ucsal.clinica.atendimento.porta.AgendamentoRepositoryPort;
import br.ucsal.clinica.atendimento.porta.AtendimentoRepositoryPort;
import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;

/**
 * Caso de uso FinalizarAtendimento (RF032, RN010): registra o encerramento e o agendamento
 * vinculado passa de AGENDADO para FINALIZADO, na mesma transacao.
 */
public class FinalizarAtendimento {

    public record Comando(String diagnostico, String tratamento, boolean internado) {
    }

    private final AtendimentoRepositoryPort atendimentos;
    private final AgendamentoRepositoryPort agendamentos;
    private final SecurityContextPort contexto;

    public FinalizarAtendimento(AtendimentoRepositoryPort atendimentos, AgendamentoRepositoryPort agendamentos,
            SecurityContextPort contexto) {
        this.atendimentos = atendimentos;
        this.agendamentos = agendamentos;
        this.contexto = contexto;
    }

    public AtendimentoClinico executar(Long id, Comando comando) {
        AtendimentoClinico atendimento = atendimentos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atendimento", id));
        atendimento.garantirResponsavel(RealizarTriagemPaciente.medicoLogado(contexto).colaboradorId());
        atendimento.finalizar(comando.diagnostico(), comando.tratamento(), comando.internado());
        AtendimentoClinico salvo = atendimentos.salvar(atendimento);
        if (atendimento.getAgendamentoId() != null) {
            agendamentos.buscarPorId(atendimento.getAgendamentoId()).ifPresent(agendamento -> {
                if (!agendamento.isFinalizado()) {
                    agendamento.finalizar(); // RN010
                    agendamentos.salvar(agendamento);
                }
            });
        }
        return salvo;
    }
}
