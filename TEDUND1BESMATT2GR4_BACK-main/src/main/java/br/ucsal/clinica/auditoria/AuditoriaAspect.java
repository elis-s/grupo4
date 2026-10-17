package br.ucsal.clinica.auditoria;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import br.ucsal.clinica.relatorios.dominio.ResultadoAuditoria;

/**
 * Auditoria passiva das consultas (AOP - secao 3.4.3 e DA014): toda consulta a
 * prontuario e toda emissao de relatorio marcada com @Auditavel gera um evento,
 * sem codigo de auditoria dentro dos casos de uso.
 */
@Aspect
@Component
public class AuditoriaAspect {

    @AfterReturning(pointcut = "@annotation(auditavel)", returning = "resultado", argNames = "execucao,auditavel,resultado")
    public void registrarConsulta(JoinPoint execucao, Auditavel auditavel, Object resultado) {
        Long id = idDoResultado(resultado);
        if (id == null) {
            for (Object arg : execucao.getArgs()) {
                if (arg instanceof Long l) {
                    id = l;
                    break;
                }
            }
        }
        String descricao = auditavel.descricao().isBlank() ? auditavel.acao().name() : auditavel.descricao();
        PublicadorAuditoria.publicar(EventoAuditoria.agora(auditavel.acao(), ResultadoAuditoria.SUCESSO,
                auditavel.entidade(), id, descricao));
    }

    /** Le o campo id() de um record de resposta, quando existir. */
    private static Long idDoResultado(Object resultado) {
        if (resultado == null || !resultado.getClass().isRecord()) {
            return null;
        }
        try {
            Method id = resultado.getClass().getMethod("id");
            Object valor = id.invoke(resultado);
            return valor instanceof Long l ? l : null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
