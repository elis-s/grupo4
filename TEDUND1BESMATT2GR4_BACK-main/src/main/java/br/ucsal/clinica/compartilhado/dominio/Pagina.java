package br.ucsal.clinica.compartilhado.dominio;

import java.util.List;
import java.util.function.Function;

/** Uma pagina de resultados: os itens e os dados para o Front-End montar a navegacao. */
public record Pagina<T>(List<T> itens, int pagina, int tamanho, long totalItens, int totalPaginas) {

    public <R> Pagina<R> map(Function<T, R> conversor) {
        return new Pagina<>(itens.stream().map(conversor).toList(), pagina, tamanho, totalItens, totalPaginas);
    }
}
