package br.ucsal.clinica.administracao.aplicacao;

import br.ucsal.clinica.administracao.dominio.Convenio;
import br.ucsal.clinica.administracao.porta.ConvenioRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;

/** Caso de uso InativarConvenio (RF009): nunca exclui, registra o motivo e preserva o historico. */
public class InativarConvenio {

    private final ConvenioRepositoryPort repositorio;

    public InativarConvenio(ConvenioRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Convenio executar(Long id, String motivo) {
        Convenio registro = repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Convênio", id));
        registro.inativar(motivo);
        return repositorio.salvar(registro);
    }
}
