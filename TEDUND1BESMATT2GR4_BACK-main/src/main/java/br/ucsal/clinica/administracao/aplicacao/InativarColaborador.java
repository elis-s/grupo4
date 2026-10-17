package br.ucsal.clinica.administracao.aplicacao;

import br.ucsal.clinica.administracao.dominio.Colaborador;
import br.ucsal.clinica.administracao.porta.ColaboradorRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;

/** Caso de uso InativarColaborador (RF012, RN006, RN025): nunca exclui, registra o motivo e preserva o historico. */
public class InativarColaborador {

    private final ColaboradorRepositoryPort repositorio;

    public InativarColaborador(ColaboradorRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Colaborador executar(Long id, String motivo) {
        Colaborador registro = repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador", id));
        registro.inativar(motivo);
        return repositorio.salvar(registro);
    }
}
