package br.ucsal.clinica.compartilhado.adaptador.persistencia;

import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;

/** Conversao entre a paginacao do dominio e a do Spring Data. */
public final class Paginas {

    private Paginas() {
    }

    public static PageRequest pedido(Paginacao paginacao, Sort ordenacao) {
        return PageRequest.of(paginacao.pagina(), paginacao.tamanho(), ordenacao);
    }

    public static <E, D> Pagina<D> converter(Page<E> page, Function<E, D> conversor) {
        return new Pagina<>(page.getContent().stream().map(conversor).toList(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
