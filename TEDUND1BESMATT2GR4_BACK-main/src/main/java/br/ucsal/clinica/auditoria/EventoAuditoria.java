package br.ucsal.clinica.auditoria;

import java.time.LocalDateTime;

import br.ucsal.clinica.relatorios.dominio.AcaoAuditoria;
import br.ucsal.clinica.relatorios.dominio.ResultadoAuditoria;

/**
 * Evento de auditoria gerado automaticamente (ouvintes JPA e aspecto das consultas).
 * Quem, quando, de onde, o que fez e sobre qual registro (RF046). E entregue ao
 * Modulo de Relatorios e Auditoria somente depois que a transacao da operacao
 * for confirmada, e gravado de forma assincrona.
 */
public record EventoAuditoria(
        String login,
        AcaoAuditoria acao,
        ResultadoAuditoria resultado,
        String entidade,
        Long entidadeId,
        String descricao,
        String ipOrigem,
        LocalDateTime dataHora) {

    private static final int TAMANHO_MAXIMO_DESCRICAO = 2000;

    /** Cria o evento com o usuario e o endereco da requisicao atual. */
    public static EventoAuditoria agora(AcaoAuditoria acao, ResultadoAuditoria resultado, String entidade,
            Long entidadeId, String descricao) {
        return comLogin(ContextoRequisicao.login(), acao, resultado, entidade, entidadeId, descricao);
    }

    public static EventoAuditoria comLogin(String login, AcaoAuditoria acao, ResultadoAuditoria resultado,
            String entidade, Long entidadeId, String descricao) {
        String texto = descricao != null && descricao.length() > TAMANHO_MAXIMO_DESCRICAO
                ? descricao.substring(0, TAMANHO_MAXIMO_DESCRICAO - 3) + "..."
                : descricao;
        String usuario = login != null && login.length() > 60 ? login.substring(0, 60) : login;
        return new EventoAuditoria(usuario, acao, resultado, entidade, entidadeId, texto,
                ContextoRequisicao.ipOrigem(), LocalDateTime.now());
    }
}
