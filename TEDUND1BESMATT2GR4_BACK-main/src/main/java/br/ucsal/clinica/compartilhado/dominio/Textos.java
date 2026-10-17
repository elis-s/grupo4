package br.ucsal.clinica.compartilhado.dominio;

/** Pequenos utilitarios de texto usados pelas entidades de dominio. */
public final class Textos {

    private Textos() {
    }

    public static String vazioParaNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public static boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    public static String obrigatorio(String valor, String mensagem) {
        if (vazio(valor)) {
            throw new RegraNegocioException(mensagem);
        }
        return valor.trim();
    }
}
