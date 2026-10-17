package br.ucsal.clinica.seguranca;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import br.ucsal.clinica.relatorios.dominio.AcaoAuditoria;
import br.ucsal.clinica.relatorios.dominio.ResultadoAuditoria;
import br.ucsal.clinica.relatorios.porta.RastroAuditoriaPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Secao 6.4, mecanismo 2 (RNF005, RNF015, DA006): chave de origem do Front-End.
 *
 * O proxy reverso do servidor do Front-End (Nginx) acrescenta em toda requisicao o
 * cabecalho X-Frontend-Key com um segredo conhecido apenas por ele e pelo Back-End
 * (lido de variavel de ambiente) - o segredo nunca chega ao codigo Angular.
 * Se o cabecalho estiver ausente ou incorreto, a requisicao e descartada com 403,
 * sem processamento e sem corpo de resposta, e a tentativa fica na auditoria.
 */
public class FrontendOriginFilter extends OncePerRequestFilter {

    public static final String CABECALHO = "X-Frontend-Key";

    private static final Logger log = LoggerFactory.getLogger(FrontendOriginFilter.class);

    private final byte[] chave;
    private final boolean exigirChave;
    private final RastroAuditoriaPort rastroAuditoria;

    public FrontendOriginFilter(String chave, boolean exigirChave, RastroAuditoriaPort rastroAuditoria) {
        if (exigirChave && (chave == null || chave.length() < 16)) {
            throw new IllegalStateException("app.frontend.chave (FRONTEND_KEY) precisa ter pelo menos 16 caracteres.");
        }
        this.chave = chave == null ? new byte[0] : chave.getBytes(StandardCharsets.UTF_8);
        this.exigirChave = exigirChave;
        this.rastroAuditoria = rastroAuditoria;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!exigirChave || chaveValida(request.getHeader(CABECALHO))) {
            chain.doFilter(request, response);
            return;
        }
        log.warn("Requisição descartada (não veio do Front-End): {} {} de {}",
                request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
        rastroAuditoria.registrarComo(null, AcaoAuditoria.ORIGEM_INVALIDA, ResultadoAuditoria.NEGADO, null, null,
                request.getMethod() + " " + request.getRequestURI() + " sem X-Frontend-Key válida");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // descartada: sem corpo de resposta
    }

    /** Comparacao em tempo constante, para nao revelar o segredo pelo tempo de resposta. */
    private boolean chaveValida(String recebida) {
        return recebida != null && MessageDigest.isEqual(recebida.getBytes(StandardCharsets.UTF_8), chave);
    }
}
