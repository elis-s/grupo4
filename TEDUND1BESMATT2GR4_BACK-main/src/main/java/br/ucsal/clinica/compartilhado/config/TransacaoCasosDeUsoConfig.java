package br.ucsal.clinica.compartilhado.config;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.Ordered;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * Secao 3.4.2 / 4.3: cada caso de uso e executado em uma unica transacao, delimitada
 * pelo adaptador de aplicacao do Spring que envolve a execucao do caso de uso.
 *
 * Como os casos de uso nao tem @Transactional (sao Java puro), a transacao e aplicada
 * aqui por configuracao: todo metodo publico de uma classe de um pacote "aplicacao"
 * roda dentro de uma transacao. Consultas e emissoes de relatorio (classes que comecam
 * com Consultar, Verificar, Validar, EmitirRelatorio ou EmitirBoletim) usam transacao somente leitura.
 */
@Configuration
public class TransacaoCasosDeUsoConfig {

    public static final String CASOS_DE_USO = "execution(public * br.ucsal.clinica..aplicacao..*(..))";

    /** A transacao fica por dentro da repeticao automatica em caso de conflito (RepeticaoEmConflitoAspect). */
    public static final int ORDEM = Ordered.LOWEST_PRECEDENCE - 100;

    private static final List<String> SOMENTE_LEITURA = List.of("Consultar", "Verificar", "Validar", "EmitirRelatorio", "EmitirBoletim");

    /**
     * O interceptador de transacao nao e publicado como bean (para nao concorrer com o do
     * Spring); ele encontra o gerenciador de transacoes (JPA) pela BeanFactory, sob demanda.
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static Advisor transacaoDosCasosDeUso(BeanFactory beanFactory) {
        TransactionInterceptor interceptador = new TransactionInterceptor();
        interceptador.setTransactionAttributeSource(new AtributosTransacao());
        interceptador.setBeanFactory(beanFactory);
        AspectJExpressionPointcut pontoDeCorte = new AspectJExpressionPointcut();
        pontoDeCorte.setExpression(CASOS_DE_USO);
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(pontoDeCorte, interceptador);
        advisor.setOrder(ORDEM);
        return advisor;
    }

    /** Toda excecao desfaz a transacao; consultas sao somente leitura. */
    static class AtributosTransacao implements TransactionAttributeSource {

        @Override
        public TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass) {
            if (method.getDeclaringClass() == Object.class) {
                return null;
            }
            RuleBasedTransactionAttribute atributo = new RuleBasedTransactionAttribute();
            atributo.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            atributo.setRollbackRules(List.of(new RollbackRuleAttribute(Exception.class)));
            String classe = (targetClass != null ? targetClass : method.getDeclaringClass()).getSimpleName();
            atributo.setReadOnly(SOMENTE_LEITURA.stream().anyMatch(classe::startsWith));
            return atributo;
        }
    }
}
