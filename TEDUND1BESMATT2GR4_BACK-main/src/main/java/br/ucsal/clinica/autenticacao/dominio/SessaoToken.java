package br.ucsal.clinica.autenticacao.dominio;

import java.time.Instant;

/** Identificacao do token usado na requisicao atual (para o logout e a renovacao). */
public record SessaoToken(String jti, Instant expiraEm) {
}
