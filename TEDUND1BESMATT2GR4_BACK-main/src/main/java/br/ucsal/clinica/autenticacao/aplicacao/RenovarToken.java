package br.ucsal.clinica.autenticacao.aplicacao;

import br.ucsal.clinica.autenticacao.dominio.CredenciaisInvalidasException;
import br.ucsal.clinica.autenticacao.dominio.SessaoToken;
import br.ucsal.clinica.autenticacao.dominio.TokenEmitido;
import br.ucsal.clinica.autenticacao.dominio.Usuario;
import br.ucsal.clinica.autenticacao.dominio.UsuarioAutenticado;
import br.ucsal.clinica.autenticacao.porta.EmissorTokenPort;
import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.autenticacao.porta.TokenRevogadoRepositoryPort;
import br.ucsal.clinica.autenticacao.porta.UsuarioRepositoryPort;
import br.ucsal.clinica.relatorios.dominio.AcaoAuditoria;
import br.ucsal.clinica.relatorios.dominio.ResultadoAuditoria;
import br.ucsal.clinica.relatorios.porta.RastroAuditoriaPort;

/**
 * Caso de uso RenovarToken (secao 6.1): enquanto o usuario estiver ativo, troca o token
 * atual (validade curta) por um novo, e o antigo passa a ser recusado.
 */
public class RenovarToken {

    private final SecurityContextPort contexto;
    private final UsuarioRepositoryPort usuarios;
    private final EmissorTokenPort emissor;
    private final TokenRevogadoRepositoryPort tokensRevogados;
    private final RastroAuditoriaPort rastro;

    public RenovarToken(SecurityContextPort contexto, UsuarioRepositoryPort usuarios, EmissorTokenPort emissor,
            TokenRevogadoRepositoryPort tokensRevogados, RastroAuditoriaPort rastro) {
        this.contexto = contexto;
        this.usuarios = usuarios;
        this.emissor = emissor;
        this.tokensRevogados = tokensRevogados;
        this.rastro = rastro;
    }

    public AutenticarUsuario.Resultado executar() {
        UsuarioAutenticado atual = contexto.usuarioAtual();
        SessaoToken sessao = contexto.sessaoAtual();
        Usuario usuario = usuarios.buscarPorLogin(atual.login())
                .orElseThrow(() -> new CredenciaisInvalidasException("Usuário não encontrado."));
        if (!usuario.podeAcessar()) {
            throw new CredenciaisInvalidasException("Usuário inativo ou vinculado a colaborador inativo.");
        }
        TokenEmitido novo = emissor.emitir(usuario);
        tokensRevogados.revogar(sessao.jti(), usuario.getId(), sessao.expiraEm());
        rastro.registrar(AcaoAuditoria.RENOVAR_TOKEN, ResultadoAuditoria.SUCESSO, "usuario", usuario.getId(),
                "Token renovado");
        return new AutenticarUsuario.Resultado(novo, UsuarioAutenticado.de(usuario));
    }
}
