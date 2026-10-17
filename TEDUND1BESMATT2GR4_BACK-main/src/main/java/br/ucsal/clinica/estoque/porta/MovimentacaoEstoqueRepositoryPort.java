package br.ucsal.clinica.estoque.porta;

import java.time.LocalDateTime;

import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.estoque.dominio.MovimentacaoEstoque;
import br.ucsal.clinica.estoque.dominio.TipoMovimentacao;

/** Porta de persistencia das movimentacoes de estoque (somente inclusao e consulta). */
public interface MovimentacaoEstoqueRepositoryPort {

    MovimentacaoEstoque registrar(MovimentacaoEstoque movimentacao);

    /** RF036 / RF043: consulta com filtros opcionais (medicamento, tipo e periodo). */
    Pagina<MovimentacaoEstoque> listar(Long medicamentoId, TipoMovimentacao tipo, LocalDateTime inicio,
            LocalDateTime fim, Paginacao paginacao);
}
