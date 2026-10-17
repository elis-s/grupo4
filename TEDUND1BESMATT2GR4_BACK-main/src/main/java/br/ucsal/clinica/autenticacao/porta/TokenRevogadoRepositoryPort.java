package br.ucsal.clinica.autenticacao.porta;

import java.time.Instant;

/** Logout (RF045): tokens revogados ficam em tb_token_revogado ate expirar (secao 6.1). */
public interface TokenRevogadoRepositoryPort {

    void revogar(String jti, Long usuarioId, Instant expiraEm);

    boolean estaRevogado(String jti);

    /** Remove os registros de tokens que ja expiraram (nao precisam mais ser recusados). */
    int removerExpirados(Instant agora);
}
