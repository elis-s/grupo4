package br.ucsal.clinica.administracao.dominio;

import br.ucsal.clinica.compartilhado.dominio.Status;

/** Especialidade medica (MapearEspecialidadeMedica - secao 3.2.2). */
public record Especialidade(Long id, String nome, Status status) {

    public boolean isAtiva() {
        return status == Status.ATIVO;
    }
}
