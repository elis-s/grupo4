package br.ucsal.clinica.estoque.dominio;

import br.ucsal.clinica.compartilhado.dominio.Status;

/** Saldo atual de um medicamento (RF057), calculado pelas movimentacoes de entrada e saida. */
public record SaldoEstoque(Long medicamentoId, String nome, String fabricante, int estoqueMinimo, long saldo,
        Status status) {

    /** RF058 / RN015 / RN016: reposicao necessaria quando o saldo atinge o nivel minimo. */
    public boolean reposicaoNecessaria() {
        return status == Status.ATIVO && saldo <= estoqueMinimo;
    }
}
