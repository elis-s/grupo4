package br.ucsal.clinica.relatorios.dominio;

import java.time.LocalDate;

/** Filtros opcionais da consulta dos logs de auditoria (RF014). */
public record FiltroAuditoria(String login, String entidade, AcaoAuditoria acao, LocalDate inicio, LocalDate fim) {
}
