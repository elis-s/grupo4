package br.ucsal.clinica.auditoria;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/** Quem esta fazendo a requisicao atual e de onde ela veio. */
public final class ContextoRequisicao {

    public static final String SISTEMA = "sistema";

    private ContextoRequisicao() {
    }

    /** Login do usuario autenticado, ou null se nao houver. */
    public static String login() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth.getName();
    }

    public static String loginOuSistema() {
        String login = login();
        return login != null ? login : SISTEMA;
    }

    /** Endereco de origem: o proxy do Front-End informa o IP do navegador em X-Forwarded-For. */
    public static String ipOrigem() {
        HttpServletRequest request = requisicaoAtual();
        if (request == null) {
            return null;
        }
        String encaminhado = request.getHeader("X-Forwarded-For");
        String ip = encaminhado != null && !encaminhado.isBlank()
                ? encaminhado.split(",")[0].trim()
                : request.getRemoteAddr();
        return ip != null && ip.length() > 45 ? ip.substring(0, 45) : ip;
    }

    public static HttpServletRequest requisicaoAtual() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        return attrs instanceof ServletRequestAttributes servlet ? servlet.getRequest() : null;
    }
}
