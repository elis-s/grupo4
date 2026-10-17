package br.ucsal.clinica.autenticacao.aplicacao;

import br.ucsal.clinica.autenticacao.dominio.CredenciaisInvalidasException;
import br.ucsal.clinica.autenticacao.dominio.TokenEmitido;
import br.ucsal.clinica.autenticacao.dominio.Usuario;
import br.ucsal.clinica.autenticacao.dominio.UsuarioAutenticado;
import br.ucsal.clinica.autenticacao.porta.EmissorTokenPort;
import br.ucsal.clinica.autenticacao.porta.UsuarioRepositoryPort;
import br.ucsal.clinica.autenticacao.porta.VerificadorCredenciaisPort;
import br.ucsal.clinica.relatorios.dominio.AcaoAuditoria;
import br.ucsal.clinica.relatorios.dominio.ResultadoAuditoria;
import br.ucsal.clinica.relatorios.porta.RastroAuditoriaPort;

/**
 * Caso de uso AutenticarUsuario (RF044, secao 6.1): confere as credenciais no banco,
 * emite o token JWT e registra o login - ou a tentativa sem sucesso - na auditoria.
 */
public class AutenticarUsuario {

    public record Resultado(TokenEmitido token, UsuarioAutenticado usuario) {
    }

    private final VerificadorCredenciaisPort verificador;
    private final UsuarioRepositoryPort usuarios;
    private final EmissorTokenPort emissor;
    private final RastroAuditoriaPort rastro;

    public AutenticarUsuario(VerificadorCredenciaisPort verificador, UsuarioRepositoryPort usuarios,
            EmissorTokenPort emissor, RastroAuditoriaPort rastro) {
        this.verificador = verificador;
        this.usuarios = usuarios;
        this.emissor = emissor;
        this.rastro = rastro;
    }

    public Resultado executar(String login, String senha) {
        try {
            verificador.verificar(login, senha);
        } catch (CredenciaisInvalidasException e) {
            rastro.registrarComo(login, AcaoAuditoria.LOGIN_FALHA, ResultadoAuditoria.FALHA, "usuario", null,
                    "Tentativa de login sem sucesso: " + e.getMessage());
            throw e;
        }
        Usuario usuario = usuarios.buscarPorLogin(login)
                .orElseThrow(() -> new CredenciaisInvalidasException("Login ou senha inválidos."));
        if (!usuario.podeAcessar()) { // RN037
            rastro.registrarComo(login, AcaoAuditoria.LOGIN_FALHA, ResultadoAuditoria.FALHA, "usuario",
                    usuario.getId(), "Usuário ou colaborador inativo");
            throw new CredenciaisInvalidasException("Usuário inativo ou vinculado a colaborador inativo.");
        }
        usuarios.registrarLogin(usuario.getId());
        TokenEmitido token = emissor.emitir(usuario);
        rastro.registrarComo(login, AcaoAuditoria.LOGIN, ResultadoAuditoria.SUCESSO, "usuario", usuario.getId(),
                "Login realizado");
        return new Resultado(token, UsuarioAutenticado.de(usuario));
    }
}
