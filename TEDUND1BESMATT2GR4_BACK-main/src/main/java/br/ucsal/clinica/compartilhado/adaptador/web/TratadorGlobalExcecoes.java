package br.ucsal.clinica.compartilhado.adaptador.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import br.ucsal.clinica.autenticacao.dominio.CredenciaisInvalidasException;
import br.ucsal.clinica.compartilhado.dominio.AcessoNegadoException;
import br.ucsal.clinica.compartilhado.dominio.ConflitoConcorrenciaException;
import br.ucsal.clinica.compartilhado.dominio.ConflitoVersaoException;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.relatorios.dominio.AcaoAuditoria;
import br.ucsal.clinica.relatorios.dominio.ResultadoAuditoria;
import br.ucsal.clinica.relatorios.porta.RastroAuditoriaPort;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Tratador global de excecoes (@RestControllerAdvice - secoes 4.2.2 e 5.2).
 *
 * Toda resposta de erro tem o mesmo formato e um codigo HTTP que indica o resultado:
 * 400 dados invalidos, 401 nao autenticado, 403 sem permissao, 404 nao encontrado,
 * 409 regra de negocio ou conflito de concorrencia e 500 erro inesperado
 * (registrado no log sem expor detalhes tecnicos ao usuario).
 */
@RestControllerAdvice
public class TratadorGlobalExcecoes {

    private static final Logger log = LoggerFactory.getLogger(TratadorGlobalExcecoes.class);

    private final RastroAuditoriaPort rastroAuditoria;

    public TratadorGlobalExcecoes(RastroAuditoriaPort rastroAuditoria) {
        this.rastroAuditoria = rastroAuditoria;
    }

    // ---------------- 409: regras de negocio e concorrencia ----------------

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResposta> regraNegocio(RegraNegocioException e, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, "Regra de negócio violada", e.getMessage(), req);
    }

    @ExceptionHandler({ ConflitoVersaoException.class, ConflitoConcorrenciaException.class })
    public ResponseEntity<ErroResposta> conflito(RuntimeException e, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, "Conflito de concorrência", e.getMessage(), req);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErroResposta> conflitoBanco(OptimisticLockingFailureException e, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, "Conflito de concorrência", new ConflitoVersaoException().getMessage(), req);
    }

    /** Regras garantidas pelo proprio banco (triggers e restricoes): saldo, duplicidades, exclusao bloqueada. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> integridade(DataIntegrityViolationException e, HttpServletRequest req) {
        return resposta(HttpStatus.CONFLICT, "Regra de negócio violada", mensagemDoBanco(e), req);
    }

    // ---------------- 404 ----------------

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> naoEncontrado(RecursoNaoEncontradoException e, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, "Registro não encontrado", e.getMessage(), req);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResposta> caminhoInexistente(NoResourceFoundException e, HttpServletRequest req) {
        return resposta(HttpStatus.NOT_FOUND, "Registro não encontrado", "Caminho inexistente na API.", req);
    }

    // ---------------- 401 e 403 ----------------

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResposta> credenciais(CredenciaisInvalidasException e, HttpServletRequest req) {
        return resposta(HttpStatus.UNAUTHORIZED, "Não autenticado", e.getMessage(), req);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResposta> autenticacao(AuthenticationException e, HttpServletRequest req) {
        return resposta(HttpStatus.UNAUTHORIZED, "Não autenticado", "Usuário não autenticado ou sessão expirada.", req);
    }

    /** RNF016: perfil sem permissao (@PreAuthorize) ou regra por dado. Fica registrado na auditoria (secao 6.7). */
    @ExceptionHandler({ AccessDeniedException.class, AcessoNegadoException.class })
    public ResponseEntity<ErroResposta> acessoNegado(RuntimeException e, HttpServletRequest req) {
        String mensagem = e instanceof AcessoNegadoException
                ? e.getMessage()
                : "Seu perfil não tem permissão para esta operação.";
        rastroAuditoria.registrar(AcaoAuditoria.ACESSO_NEGADO, ResultadoAuditoria.NEGADO, null, null,
                req.getMethod() + " " + req.getRequestURI() + " - " + mensagem);
        return resposta(HttpStatus.FORBIDDEN, "Acesso negado", mensagem, req);
    }

    // ---------------- 400: dados invalidos ----------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> validacao(MethodArgumentNotValidException e, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
        ErroResposta corpo = ErroResposta.de(400, "Dados inválidos", "Existem campos inválidos ou incompletos.",
                req.getRequestURI());
        return ResponseEntity.badRequest().body(new ErroResposta(corpo.timestamp(), corpo.status(), corpo.erro(),
                corpo.mensagem(), corpo.caminho(), campos));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> jsonInvalido(HttpMessageNotReadableException e, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Corpo da requisição inválido (verifique o formato dos campos, datas e valores permitidos).", req);
    }

    @ExceptionHandler({ MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class })
    public ResponseEntity<ErroResposta> parametroInvalido(Exception e, HttpServletRequest req) {
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos", "Parâmetro ausente ou inválido na requisição.", req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResposta> metodoInvalido(HttpRequestMethodNotSupportedException e, HttpServletRequest req) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido",
                "O método " + e.getMethod() + " não é aceito neste caminho.", req);
    }

    // ---------------- 500 ----------------

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErroResposta> banco(DataAccessException e, HttpServletRequest req) {
        log.error("Erro de banco de dados em {} {}", req.getMethod(), req.getRequestURI(), e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Não foi possível concluir a operação. Tente novamente.", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> inesperado(Exception e, HttpServletRequest req) {
        // Erros de requisicao tratados pelo proprio Spring MVC (ex.: 415 tipo de conteudo, 406)
        if (e instanceof ErrorResponse erroDoSpring && erroDoSpring.getStatusCode().is4xxClientError()) {
            int status = erroDoSpring.getStatusCode().value();
            return ResponseEntity.status(status).body(ErroResposta.de(status, "Requisição inválida",
                    "A requisição não pôde ser processada (HTTP " + status + ").", req.getRequestURI()));
        }
        log.error("Erro inesperado em {} {}", req.getMethod(), req.getRequestURI(), e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente.", req);
    }

    // ---------------- apoio ----------------

    /** Mensagem das regras do banco (triggers), sem o prefixo tecnico "ERROR:". */
    private static String mensagemDoBanco(DataAccessException e) {
        Throwable causa = e.getMostSpecificCause();
        String msg = causa != null ? causa.getMessage() : null;
        if (msg == null) {
            return "Operação recusada pelo banco de dados.";
        }
        String primeiraLinha = msg.split("\\R")[0];
        if (primeiraLinha.contains("duplicate key") || primeiraLinha.contains("duplicar valor da chave")) {
            return "Já existe um registro com estes dados.";
        }
        if (primeiraLinha.contains("violates foreign key") || primeiraLinha.contains("viola restrição de chave")) {
            return "O registro informado não existe ou está em uso por outro cadastro.";
        }
        return primeiraLinha.replaceFirst("^(ERROR|ERRO):\\s*", "");
    }

    private static ResponseEntity<ErroResposta> resposta(HttpStatus status, String erro, String mensagem,
            HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(ErroResposta.de(status.value(), erro, mensagem, req.getRequestURI()));
    }
}
