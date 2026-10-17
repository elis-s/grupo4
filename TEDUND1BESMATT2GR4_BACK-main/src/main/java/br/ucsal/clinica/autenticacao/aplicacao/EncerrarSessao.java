package br.ucsal.clinica.autenticacao.aplicacao;

import br.ucsal.clinica.autenticacao.dominio.SessaoToken;
import br.ucsal.clinica.autenticacao.dominio.UsuarioAutenticado;
import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.autenticacao.porta.TokenRevogadoRepositoryPort;
import br.ucsal.clinica.relatorios.dominio.AcaoAuditoria;
import br.ucsal.clinica.relatorios.dominio.ResultadoAuditoria;
import br.ucsal.clinica.relatorios.porta.RastroAuditoriaPort;

/**
 * Caso de uso EncerrarSessao - logout (RF045): o identificador do token e gravado em
 * tb_token_revogado ate a sua expiracao, e o filtro de autenticacao passa a recusa-lo.
 */
public class EncerrarSessao {

    private final SecurityContextPort contexto;
    private final TokenRevogadoRepositoryPort tokensRevogados;
    private final RastroAuditoriaPort rastro;

    public EncerrarSessao(SecurityContextPort contexto, TokenRevogadoRepositoryPort tokensRevogados,
            RastroAuditoriaPort rastro) {
        this.contexto = contexto;
        this.tokensRevogados = tokensRevogados;
        this.rastro = rastro;
    }

    public void executar() {
        UsuarioAutenticado usuario = contexto.usuarioAtual();
        SessaoToken sessao = contexto.sessaoAtual();
        tokensRevogados.revogar(sessao.jti(), usuario.id(), sessao.expiraEm());
        rastro.registrar(AcaoAuditoria.LOGOUT, ResultadoAuditoria.SUCESSO, "usuario", usuario.id(), "Logout");
    }
}
