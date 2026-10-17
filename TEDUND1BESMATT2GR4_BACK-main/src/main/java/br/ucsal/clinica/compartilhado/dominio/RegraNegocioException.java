package br.ucsal.clinica.compartilhado.dominio;

/** Violacao de uma regra de negocio. A API responde 409 (secao 4.2.2). */
public class RegraNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
