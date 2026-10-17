package br.ucsal.clinica.seguranca;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import br.ucsal.clinica.autenticacao.dominio.TokenEmitido;
import br.ucsal.clinica.autenticacao.dominio.Usuario;
import br.ucsal.clinica.autenticacao.porta.EmissorTokenPort;
import br.ucsal.clinica.compartilhado.dominio.Referencia;

/** Implementacao da porta de emissao de token com o JwtEncoder do Spring Security (HS256). */
@Component
public class EmissorTokenJwtAdapter implements EmissorTokenPort {

    private final JwtEncoder encoder;
    private final long minutosValidade;
    private final String emissor;

    public EmissorTokenJwtAdapter(JwtEncoder encoder,
            @Value("${app.jwt.expiracao-minutos:30}") long minutosValidade,
            @Value("${app.jwt.emissor:clinica-backend}") String emissor) {
        this.encoder = encoder;
        this.minutosValidade = minutosValidade;
        this.emissor = emissor;
    }

    @Override
    public TokenEmitido emitir(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(minutosValidade, ChronoUnit.MINUTES);
        String jti = UUID.randomUUID().toString();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(emissor)
                .subject(usuario.getLogin())
                .id(jti)
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .claim("uid", usuario.getId())
                .claim("nome", Referencia.nomeDe(usuario.getColaborador()))
                .claim(JwtConfig.CLAIM_PERFIS, usuario.getPerfis().stream().map(Enum::name).sorted().toList())
                .build();
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
        return new TokenEmitido(token, jti, expiraEm);
    }
}
