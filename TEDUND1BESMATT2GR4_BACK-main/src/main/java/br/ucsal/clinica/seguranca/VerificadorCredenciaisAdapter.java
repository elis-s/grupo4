package br.ucsal.clinica.seguranca;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import br.ucsal.clinica.autenticacao.dominio.CredenciaisInvalidasException;
import br.ucsal.clinica.autenticacao.porta.VerificadorCredenciaisPort;

/**
 * Secao 6.1, passo 3: o AuthenticationManager do Spring Security usa o
 * DaoAuthenticationProvider, que busca o usuario no PostgreSQL e compara a senha
 * com o hash BCrypt.
 */
@Component
public class VerificadorCredenciaisAdapter implements VerificadorCredenciaisPort {

    private final AuthenticationManager authenticationManager;

    public VerificadorCredenciaisAdapter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void verificar(String login, String senha) {
        try {
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(login, senha));
        } catch (DisabledException e) {
            throw new CredenciaisInvalidasException("Usuário inativo ou vinculado a colaborador inativo.");
        } catch (AuthenticationException e) {
            throw new CredenciaisInvalidasException("Login ou senha inválidos.");
        }
    }
}
