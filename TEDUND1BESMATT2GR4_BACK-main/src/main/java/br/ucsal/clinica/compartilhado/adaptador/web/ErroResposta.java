package br.ucsal.clinica.compartilhado.adaptador.web;

import java.time.OffsetDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Formato padronizado das respostas de erro (secao 4.2.2 - RNF023):
 * { "timestamp", "status", "erro", "mensagem", "caminho" } e, nos erros de
 * validacao, os campos invalidos.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResposta(
        OffsetDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        Map<String, String> campos) {

    public static ErroResposta de(int status, String erro, String mensagem, String caminho) {
        return new ErroResposta(OffsetDateTime.now(), status, erro, mensagem, caminho, null);
    }
}
