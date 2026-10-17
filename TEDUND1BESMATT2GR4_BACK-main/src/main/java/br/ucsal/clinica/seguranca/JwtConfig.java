package br.ucsal.clinica.seguranca;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * Token JWT (secao 6.1, DA008): assinado com HMAC-SHA256, com o identificador do
 * usuario, os perfis e a data de expiracao. A chave de assinatura vem de variavel de
 * ambiente (secao 6.5). Na leitura, alem da assinatura e da validade, o token precisa
 * nao estar revogado (logout) e o usuario precisa continuar ativo (TokenAtivoValidator).
 */
@Configuration
public class JwtConfig {

    public static final String CLAIM_PERFIS = "perfis";

    @Value("${app.jwt.segredo}")
    private String segredo;

    @Value("${app.jwt.emissor:clinica-backend}")
    private String emissor;

    private SecretKey chave() {
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.segredo (JWT_SECRET) precisa ter pelo menos 32 caracteres.");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chave()));
    }

    @Bean
    public JwtDecoder jwtDecoder(TokenAtivoValidator tokenAtivo) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(chave()).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(emissor), tokenAtivo));
        return decoder;
    }

    /** Le os perfis do token e transforma em ROLE_ADMINISTRADOR, ROLE_COLABORADOR e ROLE_USUARIO. */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter perfis = new JwtGrantedAuthoritiesConverter();
        perfis.setAuthoritiesClaimName(CLAIM_PERFIS);
        perfis.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(perfis);
        return conversor;
    }
}
