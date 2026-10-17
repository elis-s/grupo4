package br.ucsal.clinica.autenticacao.aplicacao;

import java.time.Instant;

import br.ucsal.clinica.autenticacao.porta.TokenRevogadoRepositoryPort;

/** Remove de tb_token_revogado os tokens que ja expiraram (nao precisam mais ser recusados). */
public class LimparTokensExpirados {

    private final TokenRevogadoRepositoryPort tokensRevogados;

    public LimparTokensExpirados(TokenRevogadoRepositoryPort tokensRevogados) {
        this.tokensRevogados = tokensRevogados;
    }

    public int executar() {
        return tokensRevogados.removerExpirados(Instant.now());
    }
}
