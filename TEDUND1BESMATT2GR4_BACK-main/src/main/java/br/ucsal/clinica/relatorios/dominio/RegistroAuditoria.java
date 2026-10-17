package br.ucsal.clinica.relatorios.dominio;

import java.time.LocalDateTime;

/**
 * Registro de auditoria (RF046, RN036): usuario responsavel, data e hora, endereco de
 * origem, operacao executada, resultado e registro afetado. Imutavel: so e incluido e consultado.
 */
public record RegistroAuditoria(
        Long id,
        Long usuarioId,
        String login,
        AcaoAuditoria acao,
        ResultadoAuditoria resultado,
        String entidade,
        Long entidadeId,
        String descricao,
        String ipOrigem,
        LocalDateTime dataHora) {
}
