package br.ucsal.clinica.relatorios.dominio;

import java.time.LocalDateTime;
import java.util.List;

/** RN034: identificacao de quem emitiu o relatorio e quando. */
public record EmissorRelatorio(String login, String nome, List<String> perfis, LocalDateTime dataEmissao) {
}
