package br.ucsal.clinica.auditoria;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Ativa o AuditingEntityListener do Spring Data (secao 3.4.3): ao inserir ou alterar
 * um registro, as colunas criado_por, atualizado_por, data_cadastro e
 * data_atualizacao sao preenchidas com o usuario autenticado e a data/hora.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "usuarioAuditor", modifyOnCreate = false)
public class AuditoriaJpaConfig {

    @Bean
    public AuditorAware<String> usuarioAuditor() {
        return () -> Optional.of(ContextoRequisicao.loginOuSistema());
    }
}
