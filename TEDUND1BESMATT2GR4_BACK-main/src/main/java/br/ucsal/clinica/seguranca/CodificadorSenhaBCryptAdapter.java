package br.ucsal.clinica.seguranca;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.ucsal.clinica.autenticacao.porta.CodificadorSenhaPort;

/** RNF013: as senhas sao gravadas somente como hash BCrypt (BCryptPasswordEncoder). */
@Component
public class CodificadorSenhaBCryptAdapter implements CodificadorSenhaPort {

    private final PasswordEncoder passwordEncoder;

    public CodificadorSenhaBCryptAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String codificar(String senha) {
        return passwordEncoder.encode(senha);
    }

    @Override
    public boolean confere(String senha, String hash) {
        return senha != null && hash != null && passwordEncoder.matches(senha, hash);
    }
}
