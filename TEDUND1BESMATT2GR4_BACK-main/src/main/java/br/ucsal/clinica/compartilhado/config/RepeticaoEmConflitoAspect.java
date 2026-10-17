package br.ucsal.clinica.compartilhado.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import br.ucsal.clinica.compartilhado.dominio.ConflitoConcorrenciaException;

/**
 * DA013 / RNF021: bloqueio otimista no controle de estoque com repeticao da transacao.
 *
 * Se duas movimentacoes simultaneas do mesmo medicamento entrarem em conflito
 * (a versao do medicamento mudou), a operacao inteira e repetida, agora com o saldo
 * revalidado. Depois de 3 tentativas, o usuario recebe 409.
 *
 * Este aspecto fica POR FORA da transacao (ordem menor), para que cada tentativa
 * seja uma transacao nova.
 */
@Aspect
@Component
@Order(TransacaoCasosDeUsoConfig.ORDEM - 10)
public class RepeticaoEmConflitoAspect {

    private static final Logger log = LoggerFactory.getLogger(RepeticaoEmConflitoAspect.class);

    static final int TENTATIVAS = 3;

    @Around("execution(public * br.ucsal.clinica.estoque.aplicacao.RegistrarRetiradaMedicamento.*(..))"
            + " || execution(public * br.ucsal.clinica.estoque.aplicacao.RegistrarReposicao.*(..))"
            + " || execution(public * br.ucsal.clinica.estoque.aplicacao.CadastrarMedicamento.*(..))")
    public Object repetirEmConflito(ProceedingJoinPoint execucao) throws Throwable {
        for (int tentativa = 1;; tentativa++) {
            try {
                return execucao.proceed();
            } catch (RuntimeException e) {
                if (!conflitoDeConcorrencia(e)) {
                    throw e;
                }
                if (tentativa >= TENTATIVAS) {
                    log.warn("Conflito de concorrência em {} após {} tentativas",
                            execucao.getSignature().toShortString(), tentativa);
                    throw new ConflitoConcorrenciaException();
                }
                log.info("Conflito de concorrência em {}; repetindo (tentativa {})",
                        execucao.getSignature().toShortString(), tentativa + 1);
            }
        }
    }

    /** Procura na cadeia de causas uma falha de bloqueio otimista (Spring, JPA ou Hibernate). */
    static boolean conflitoDeConcorrencia(Throwable erro) {
        Throwable atual = erro;
        while (atual != null) {
            String nome = atual.getClass().getSimpleName();
            if (atual instanceof OptimisticLockingFailureException || nome.contains("OptimisticLock")
                    || nome.contains("StaleObjectState") || nome.contains("StaleState")) {
                return true;
            }
            atual = atual.getCause() == atual ? null : atual.getCause();
        }
        return false;
    }
}
