package br.ucsal.clinica.auditoria;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.ucsal.clinica.relatorios.dominio.AcaoAuditoria;

/**
 * Secao 3.4.3: as leituras de dados clinicos e a emissao de relatorios nao passam
 * pelos ouvintes de entidade JPA, entao sao capturadas pelo aspecto (AOP) aplicado
 * aos pontos de entrada dessas consultas.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditavel {

    AcaoAuditoria acao();

    String entidade();

    String descricao() default "";
}
