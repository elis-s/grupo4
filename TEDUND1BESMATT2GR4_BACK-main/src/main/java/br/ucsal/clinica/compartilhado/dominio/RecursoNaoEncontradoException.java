package br.ucsal.clinica.compartilhado.dominio;

/** Registro procurado nao existe. A API responde 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RecursoNaoEncontradoException(String entidade, Long id) {
        super(entidade + " com id " + id + " não encontrado(a).");
    }

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
