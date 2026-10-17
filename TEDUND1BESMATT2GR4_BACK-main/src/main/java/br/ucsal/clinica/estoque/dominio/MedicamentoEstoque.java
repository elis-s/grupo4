package br.ucsal.clinica.estoque.dominio;

/** Medicamento acompanhado do saldo atual em estoque (RF034, RF057, RF058). */
public record MedicamentoEstoque(Medicamento medicamento, long saldo) {

    public boolean reposicaoNecessaria() {
        return medicamento.isAtivo() && saldo <= medicamento.getEstoqueMinimo();
    }
}
