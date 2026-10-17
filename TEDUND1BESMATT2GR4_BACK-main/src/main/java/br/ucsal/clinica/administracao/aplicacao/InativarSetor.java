package br.ucsal.clinica.administracao.aplicacao;

import br.ucsal.clinica.administracao.dominio.Setor;
import br.ucsal.clinica.administracao.porta.SetorRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;

/** Caso de uso InativarSetor (RF006): mantem o historico no sistema. */
public class InativarSetor {

    private final SetorRepositoryPort repositorio;

    public InativarSetor(SetorRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Setor executar(Long id, String motivo) {
        Setor setor = repositorio.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Setor", id));
        setor.inativar(motivo);
        return repositorio.salvar(setor);
    }
}
