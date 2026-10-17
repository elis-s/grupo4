package br.ucsal.clinica.estoque.aplicacao;

import java.time.LocalDate;

import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.estoque.dominio.MovimentacaoEstoque;
import br.ucsal.clinica.estoque.dominio.TipoMovimentacao;
import br.ucsal.clinica.estoque.porta.MovimentacaoEstoqueRepositoryPort;

/** Consulta das movimentacoes de entrada e saida do estoque (RF036, RF039, RF043). */
public class ConsultarMovimentacoes {

    private final MovimentacaoEstoqueRepositoryPort movimentacoes;

    public ConsultarMovimentacoes(MovimentacaoEstoqueRepositoryPort movimentacoes) {
        this.movimentacoes = movimentacoes;
    }

    public Pagina<MovimentacaoEstoque> listar(Long medicamentoId, TipoMovimentacao tipo, LocalDate inicio,
            LocalDate fim, Paginacao paginacao) {
        if (inicio != null && fim != null && fim.isBefore(inicio)) {
            throw new RegraNegocioException("A data final não pode ser anterior à data inicial.");
        }
        return movimentacoes.listar(medicamentoId, tipo, inicio == null ? null : inicio.atStartOfDay(),
                fim == null ? null : fim.plusDays(1).atStartOfDay(), paginacao);
    }
}
