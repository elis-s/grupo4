package br.ucsal.clinica.autenticacao.porta;

/** Hash seguro das senhas (BCrypt - RNF013). */
public interface CodificadorSenhaPort {

    String codificar(String senha);

    boolean confere(String senha, String hash);
}
