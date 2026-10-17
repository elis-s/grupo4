package br.ucsal.clinica.compartilhado.adaptador.web;

import jakarta.validation.constraints.NotBlank;

/** Corpo de toda requisicao de inativacao: o motivo e obrigatorio (RN023). */
public record InativacaoRequest(
        @NotBlank(message = "Informe o motivo da inativação.") String motivo) {
}
