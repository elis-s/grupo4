package br.ucsal.clinica.seguranca;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import br.ucsal.clinica.auditoria.ContextoRequisicao;
import br.ucsal.clinica.autenticacao.dominio.CredenciaisInvalidasException;
import br.ucsal.clinica.autenticacao.dominio.SessaoToken;
import br.ucsal.clinica.autenticacao.dominio.UsuarioAutenticado;
import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.autenticacao.porta.UsuarioRepositoryPort;

/**
 * Implementacao da SecurityContextPort (secao 3.2.1): traduz o contexto do Spring
 * Security (token JWT) para o ator autenticado do dominio, sem que os casos de uso
 * dependam do Spring.
 */
@Component
public class SecurityContextAdapter implements SecurityContextPort {

    private final UsuarioRepositoryPort usuarios;

    public SecurityContextAdapter(UsuarioRepositoryPort usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UsuarioAutenticado usuarioAtual() {
        String login = ContextoRequisicao.login();
        if (login == null) {
            throw new CredenciaisInvalidasException("Usuário não autenticado.");
        }
        return usuarios.buscarPorLogin(login)
                .map(UsuarioAutenticado::de)
                .orElseThrow(() -> new CredenciaisInvalidasException("Usuário não encontrado."));
    }

    @Override
    public SessaoToken sessaoAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken tokenJwt) {
            Jwt jwt = tokenJwt.getToken();
            return new SessaoToken(jwt.getId(), jwt.getExpiresAt());
        }
        throw new CredenciaisInvalidasException("Sessão sem token de acesso.");
    }
}
