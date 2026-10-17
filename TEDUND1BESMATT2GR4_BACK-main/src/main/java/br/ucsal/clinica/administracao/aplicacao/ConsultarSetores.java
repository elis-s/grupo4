package br.ucsal.clinica.administracao.aplicacao;

import br.ucsal.clinica.administracao.dominio.Setor;
import br.ucsal.clinica.administracao.porta.SetorRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Status;

/** Caso de uso ConsultarSetores (RF004): consulta por id e listagem paginada. */
public class ConsultarSetores {

    private final SetorRepositoryPort repositorio;

    public ConsultarSetores(SetorRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Setor porId(Long id) {
        return repositorio.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Setor", id));
    }

    public Pagina<Setor> listar(Status status, Paginacao paginacao) {
        return repositorio.listar(status, paginacao);
    }
}
