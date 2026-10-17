package br.ucsal.clinica.administracao.aplicacao;

import br.ucsal.clinica.administracao.dominio.Clinica;
import br.ucsal.clinica.administracao.porta.ClinicaRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Status;

/** Caso de uso ConsultarClinicas (RF001): consulta por id e listagem paginada. */
public class ConsultarClinicas {

    private final ClinicaRepositoryPort repositorio;

    public ConsultarClinicas(ClinicaRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Clinica porId(Long id) {
        return repositorio.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Clínica", id));
    }

    public Pagina<Clinica> listar(Status status, Paginacao paginacao) {
        return repositorio.listar(status, paginacao);
    }
}
