package br.ucsal.clinica.administracao.aplicacao;

import br.ucsal.clinica.administracao.dominio.Clinica;
import br.ucsal.clinica.administracao.porta.ClinicaRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;

/** Caso de uso ReativarClinica (RF003): volta a usar um registro inativado. */
public class ReativarClinica {

    private final ClinicaRepositoryPort repositorio;

    public ReativarClinica(ClinicaRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Clinica executar(Long id) {
        Clinica registro = repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Clínica", id));
        registro.reativar();
        return repositorio.salvar(registro);
    }
}
