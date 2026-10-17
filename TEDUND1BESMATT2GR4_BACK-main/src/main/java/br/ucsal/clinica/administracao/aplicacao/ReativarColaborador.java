package br.ucsal.clinica.administracao.aplicacao;

import br.ucsal.clinica.administracao.dominio.Colaborador;
import br.ucsal.clinica.administracao.porta.ColaboradorRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;

/** Caso de uso ReativarColaborador (RF012): volta a usar um registro inativado. */
public class ReativarColaborador {

    private final ColaboradorRepositoryPort repositorio;

    public ReativarColaborador(ColaboradorRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Colaborador executar(Long id) {
        Colaborador registro = repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador", id));
        registro.reativar();
        return repositorio.salvar(registro);
    }
}
