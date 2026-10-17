package br.ucsal.clinica.administracao.aplicacao;

import br.ucsal.clinica.administracao.dominio.Convenio;
import br.ucsal.clinica.administracao.porta.ConvenioRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;

/** Caso de uso ReativarConvenio (RF009): volta a usar um registro inativado. */
public class ReativarConvenio {

    private final ConvenioRepositoryPort repositorio;

    public ReativarConvenio(ConvenioRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Convenio executar(Long id) {
        Convenio registro = repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Convênio", id));
        registro.reativar();
        return repositorio.salvar(registro);
    }
}
