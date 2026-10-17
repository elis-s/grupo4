package br.ucsal.clinica.auditoria;

/**
 * Evento de seguranca (login, falha de login, logout, origem invalida, acesso negado).
 * Diferente do EventoAuditoria, e gravado mesmo que a requisicao termine em erro,
 * pois justamente as tentativas recusadas precisam ficar registradas (RNF015, secao 6.7).
 */
public record EventoSeguranca(EventoAuditoria dados) {
}
