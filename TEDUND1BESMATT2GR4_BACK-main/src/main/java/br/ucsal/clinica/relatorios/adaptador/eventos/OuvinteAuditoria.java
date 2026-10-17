package br.ucsal.clinica.relatorios.adaptador.eventos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import br.ucsal.clinica.auditoria.EventoAuditoria;
import br.ucsal.clinica.auditoria.EventoSeguranca;
import br.ucsal.clinica.relatorios.aplicacao.CapturarRastroAuditoria;
import br.ucsal.clinica.relatorios.dominio.RegistroAuditoria;

/**
 * O Modulo de Relatorios e Auditoria como ouvinte passivo de eventos (Domain Events
 * Listener - secao 3.2.6). A gravacao e assincrona, para nao penalizar a latencia das
 * transacoes clinicas.
 *
 *  - EventoAuditoria (cadastros, edicoes, status, consultas): gravado so depois que a
 *    transacao da operacao for confirmada; se ela for desfeita, nada e registrado.
 *  - EventoSeguranca (login, falha de login, logout, origem invalida, acesso negado):
 *    gravado mesmo quando a requisicao termina em erro.
 */
@Component
public class OuvinteAuditoria {

    private static final Logger log = LoggerFactory.getLogger(OuvinteAuditoria.class);

    private final CapturarRastroAuditoria capturar;

    public OuvinteAuditoria(CapturarRastroAuditoria capturar) {
        this.capturar = capturar;
    }

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    public void aposConfirmarOperacao(EventoAuditoria evento) {
        gravar(evento);
    }

    @Async
    @EventListener
    public void aoOcorrerEventoSeguranca(EventoSeguranca evento) {
        gravar(evento.dados());
    }

    private void gravar(EventoAuditoria e) {
        try {
            capturar.executar(new RegistroAuditoria(null, null, e.login(), e.acao(), e.resultado(), e.entidade(),
                    e.entidadeId(), e.descricao(), e.ipOrigem(), e.dataHora()));
        } catch (RuntimeException erro) {
            log.error("Falha ao gravar a auditoria ({} {} {}): {}", e.acao(), e.entidade(), e.entidadeId(),
                    erro.getMessage());
        }
    }
}
