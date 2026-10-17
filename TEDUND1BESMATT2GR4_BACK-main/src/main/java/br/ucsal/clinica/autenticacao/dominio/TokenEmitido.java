package br.ucsal.clinica.autenticacao.dominio;

import java.time.Instant;

/** Token JWT emitido no login ou na renovacao: o identificador (jti) permite revoga-lo no logout. */
public record TokenEmitido(String token, String jti, Instant expiraEm) {
}
