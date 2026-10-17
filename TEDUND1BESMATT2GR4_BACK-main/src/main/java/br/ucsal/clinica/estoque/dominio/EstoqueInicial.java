package br.ucsal.clinica.estoque.dominio;

import java.time.LocalDate;

/** Quantidade em estoque informada no cadastro do medicamento (RF033): vira a primeira entrada. */
public record EstoqueInicial(int quantidade, String lote, LocalDate dataValidade, LocalDate dataAquisicao) {
}
