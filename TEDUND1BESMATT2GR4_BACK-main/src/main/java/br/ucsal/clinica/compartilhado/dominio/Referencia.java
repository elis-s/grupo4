package br.ucsal.clinica.compartilhado.dominio;

/**
 * Referencia a uma entidade de outro agregado ou modulo: o dominio guarda so o
 * identificador e o nome para exibicao, sem depender da entidade inteira.
 */
public record Referencia(Long id, String nome) {

    public static Referencia de(Long id) {
        return id == null ? null : new Referencia(id, null);
    }

    public static Long idDe(Referencia referencia) {
        return referencia == null ? null : referencia.id();
    }

    public static String nomeDe(Referencia referencia) {
        return referencia == null ? null : referencia.nome();
    }
}
