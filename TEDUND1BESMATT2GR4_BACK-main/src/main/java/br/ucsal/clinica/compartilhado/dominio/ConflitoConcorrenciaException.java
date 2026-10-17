package br.ucsal.clinica.compartilhado.dominio;

/**
 * DA013: a operacao de estoque foi repetida automaticamente e continuou em conflito
 * com outra movimentacao simultanea. A API responde 409.
 */
public class ConflitoConcorrenciaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConflitoConcorrenciaException() {
        super("Outra operação alterou este registro ao mesmo tempo. Tente novamente.");
    }
}
