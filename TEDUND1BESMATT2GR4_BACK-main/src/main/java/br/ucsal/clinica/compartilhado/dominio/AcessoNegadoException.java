package br.ucsal.clinica.compartilhado.dominio;

/**
 * Autorizacao por dado (secao 6.2, nivel 3): o caso de uso comparou o usuario
 * autenticado com o registro e negou o acesso. A API responde 403.
 */
public class AcessoNegadoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
