package br.ucsal.clinica.seguranca;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import br.ucsal.clinica.autenticacao.aplicacao.ValidarTokenAcesso;

/**
 * Adaptador do Spring Security para o caso de uso ValidarTokenAcesso: e chamado na leitura
 * de todo token recebido, depois da conferencia da assinatura e da validade.
 */
@Component
public class TokenAtivoValidator implements OAuth2TokenValidator<Jwt> {

    private final ValidarTokenAcesso validarTokenAcesso;

    public TokenAtivoValidator(ValidarTokenAcesso validarTokenAcesso) {
        this.validarTokenAcesso = validarTokenAcesso;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        ValidarTokenAcesso.Resultado resultado = validarTokenAcesso.executar(token.getId(), token.getSubject());
        if (resultado.valido()) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", resultado.motivo(), null));
    }
}
