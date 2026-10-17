package br.ucsal.clinica.compartilhado.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita a gravacao assincrona da auditoria (secao 3.2.6: nao penaliza a
 * latencia das transacoes clinicas) e a tarefa agendada de limpeza dos
 * tokens revogados ja expirados.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class ExecucaoAssincronaConfig {
}
