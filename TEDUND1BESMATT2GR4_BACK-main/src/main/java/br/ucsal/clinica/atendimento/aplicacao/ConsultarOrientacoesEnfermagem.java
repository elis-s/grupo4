package br.ucsal.clinica.atendimento.aplicacao;

import java.util.List;

import br.ucsal.clinica.atendimento.dominio.OrientacaoEnfermagem;
import br.ucsal.clinica.atendimento.porta.AtendimentoRepositoryPort;
import br.ucsal.clinica.atendimento.porta.OrientacaoEnfermagemRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;

/** Consulta das orientacoes de enfermagem de um atendimento. */
public class ConsultarOrientacoesEnfermagem {

    private final AtendimentoRepositoryPort atendimentos;
    private final OrientacaoEnfermagemRepositoryPort orientacoes;

    public ConsultarOrientacoesEnfermagem(AtendimentoRepositoryPort atendimentos,
            OrientacaoEnfermagemRepositoryPort orientacoes) {
        this.atendimentos = atendimentos;
        this.orientacoes = orientacoes;
    }

    public List<OrientacaoEnfermagem> doAtendimento(Long atendimentoId) {
        atendimentos.buscarPorId(atendimentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atendimento", atendimentoId));
        return orientacoes.listarDoAtendimento(atendimentoId);
    }
}
