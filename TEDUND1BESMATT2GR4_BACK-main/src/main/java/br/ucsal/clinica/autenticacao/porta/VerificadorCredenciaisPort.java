package br.ucsal.clinica.autenticacao.porta;

/**
 * Confere login e senha. Implementada com o AuthenticationManager do Spring Security,
 * que usa o DaoAuthenticationProvider e os usuarios do PostgreSQL (secao 6.1).
 */
public interface VerificadorCredenciaisPort {

    /** @throws CredenciaisInvalidasException se o login, a senha ou a situacao do usuario impedirem o acesso */
    void verificar(String login, String senha);
}
