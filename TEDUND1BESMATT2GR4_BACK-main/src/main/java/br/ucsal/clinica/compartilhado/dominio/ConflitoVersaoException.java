package br.ucsal.clinica.compartilhado.dominio;

/** O registro foi alterado por outro usuario depois de ser lido (bloqueio otimista). A API responde 409. */
public class ConflitoVersaoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConflitoVersaoException() {
        super("Este registro foi alterado por outro usuário. Recarregue a tela e tente novamente.");
    }
}
