package br.ucsal.clinica.relatorios.dominio;

/** Operacoes registradas em tb_auditoria (RF046 e secao 6.7). */
public enum AcaoAuditoria {
    LOGIN,
    LOGIN_FALHA,
    LOGOUT,
    RENOVAR_TOKEN,
    ORIGEM_INVALIDA,
    ACESSO_NEGADO,
    CRIAR,
    EDITAR,
    INATIVAR,
    REATIVAR,
    MUDAR_STATUS,
    CONSULTAR,
    EMITIR_RELATORIO
}
