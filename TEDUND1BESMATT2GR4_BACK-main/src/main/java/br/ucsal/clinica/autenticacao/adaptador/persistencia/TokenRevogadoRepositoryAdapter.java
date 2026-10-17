package br.ucsal.clinica.autenticacao.adaptador.persistencia;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

import br.ucsal.clinica.autenticacao.porta.TokenRevogadoRepositoryPort;

/** Adaptador JPA da porta dos tokens revogados (logout - RF045). */
@Component
public class TokenRevogadoRepositoryAdapter implements TokenRevogadoRepositoryPort {

    private final TokenRevogadoJpaRepository jpa;

    public TokenRevogadoRepositoryAdapter(TokenRevogadoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void revogar(String jti, Long usuarioId, Instant expiraEm) {
        if (jti == null || jpa.existsByJti(jti)) {
            return;
        }
        TokenRevogadoEntity e = new TokenRevogadoEntity();
        e.setJti(jti);
        e.setUsuarioId(usuarioId);
        e.setDataExpiracao(LocalDateTime.ofInstant(expiraEm, ZoneId.systemDefault()));
        e.setDataRevogacao(LocalDateTime.now());
        jpa.saveAndFlush(e);
    }

    @Override
    public boolean estaRevogado(String jti) {
        return jti != null && jpa.existsByJti(jti);
    }

    @Override
    public int removerExpirados(Instant agora) {
        return jpa.removerExpirados(LocalDateTime.ofInstant(agora, ZoneId.systemDefault()));
    }
}
