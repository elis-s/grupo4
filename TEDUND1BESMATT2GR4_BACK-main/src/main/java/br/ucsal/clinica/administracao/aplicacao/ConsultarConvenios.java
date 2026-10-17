package br.ucsal.clinica.administracao.aplicacao;

import br.ucsal.clinica.administracao.dominio.Convenio;
import br.ucsal.clinica.administracao.porta.ConvenioRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Status;

/** Caso de uso ConsultarConvenios (RF007): consulta por id e listagem paginada. */
public class ConsultarConvenios {

    private final ConvenioRepositoryPort repositorio;

    public ConsultarConvenios(ConvenioRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Convenio porId(Long id) {
        return repositorio.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Convênio", id));
    }

    public Pagina<Convenio> listar(Status status, Paginacao paginacao) {
        return repositorio.listar(status, paginacao);
    }
}
