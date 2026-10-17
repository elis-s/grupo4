package br.ucsal.clinica.autenticacao.dominio;

/** Login recusado (usuario ou senha invalidos, usuario inativo) ou token invalido. A API responde 401. */
public class CredenciaisInvalidasException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }
}
