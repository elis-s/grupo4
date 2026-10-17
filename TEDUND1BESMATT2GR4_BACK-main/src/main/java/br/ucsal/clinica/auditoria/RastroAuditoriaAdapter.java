package br.ucsal.clinica.auditoria;

import org.springframework.stereotype.Component;

import br.ucsal.clinica.relatorios.dominio.AcaoAuditoria;
import br.ucsal.clinica.relatorios.dominio.ResultadoAuditoria;
import br.ucsal.clinica.relatorios.porta.RastroAuditoriaPort;

/** Implementacao da porta de rastro de auditoria: envia eventos de seguranca. */
@Component
public class RastroAuditoriaAdapter implements RastroAuditoriaPort {

    @Override
    public void registrar(AcaoAuditoria acao, ResultadoAuditoria resultado, String entidade, Long entidadeId,
            String descricao) {
        PublicadorAuditoria.publicar(new EventoSeguranca(
                EventoAuditoria.agora(acao, resultado, entidade, entidadeId, descricao)));
    }

    @Override
    public void registrarComo(String login, AcaoAuditoria acao, ResultadoAuditoria resultado, String entidade,
            Long entidadeId, String descricao) {
        PublicadorAuditoria.publicar(new EventoSeguranca(
                EventoAuditoria.comLogin(login, acao, resultado, entidade, entidadeId, descricao)));
    }
}
