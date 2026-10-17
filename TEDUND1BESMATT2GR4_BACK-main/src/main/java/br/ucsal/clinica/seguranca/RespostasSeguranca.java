package br.ucsal.clinica.seguranca;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import br.ucsal.clinica.relatorios.dominio.AcaoAuditoria;
import br.ucsal.clinica.relatorios.dominio.ResultadoAuditoria;
import br.ucsal.clinica.relatorios.porta.RastroAuditoriaPort;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Respostas padronizadas (secao 4.2.2) dos bloqueios feitos pelos filtros do Spring Security:
 * 401 para token ausente, invalido, expirado ou revogado, e 403 para perfil sem
 * permissao no caminho - este ultimo registrado na auditoria (secao 6.7).
 */
@Component
public class RespostasSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final RastroAuditoriaPort rastroAuditoria;

    public RespostasSeguranca(RastroAuditoriaPort rastroAuditoria) {
        this.rastroAuditoria = rastroAuditoria;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        response.setHeader("WWW-Authenticate", "Bearer");
        escrever(response, request, 401, "Não autenticado",
                "Token de acesso ausente, inválido, expirado ou revogado. Faça login novamente.");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        rastroAuditoria.registrar(AcaoAuditoria.ACESSO_NEGADO, ResultadoAuditoria.NEGADO, null, null,
                request.getMethod() + " " + request.getRequestURI() + " - perfil sem permissão");
        escrever(response, request, 403, "Acesso negado", "Seu perfil não tem permissão para esta operação.");
    }

    private static void escrever(HttpServletResponse response, HttpServletRequest request, int status, String erro,
            String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"timestamp\":\"" + OffsetDateTime.now() + "\",\"status\":" + status
                + ",\"erro\":\"" + json(erro) + "\",\"mensagem\":\"" + json(mensagem)
                + "\",\"caminho\":\"" + json(request.getRequestURI()) + "\"}");
    }

    private static String json(String texto) {
        return texto == null ? "" : texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
