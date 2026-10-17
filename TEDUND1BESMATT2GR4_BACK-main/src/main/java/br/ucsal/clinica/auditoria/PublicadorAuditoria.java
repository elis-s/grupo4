package br.ucsal.clinica.auditoria;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

/**
 * Publica os eventos de auditoria para o Modulo de Relatorios e Auditoria, que atua
 * como ouvinte passivo (Domain Events Listener - secao 3.2.6).
 *
 * Os ouvintes de entidade JPA sao criados pelo Hibernate, entao o publicador fica
 * disponivel tambem por um acesso estatico.
 */
@Component
public class PublicadorAuditoria implements ApplicationEventPublisherAware {

    private static volatile ApplicationEventPublisher publicador;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        PublicadorAuditoria.publicador = applicationEventPublisher;
    }

    public static void publicar(Object evento) {
        ApplicationEventPublisher atual = publicador;
        if (atual != null) {
            atual.publishEvent(evento);
        }
    }
}
