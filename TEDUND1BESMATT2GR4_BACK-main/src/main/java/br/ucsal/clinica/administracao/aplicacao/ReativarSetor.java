package br.ucsal.clinica.administracao.aplicacao;

import br.ucsal.clinica.administracao.dominio.Setor;
import br.ucsal.clinica.administracao.porta.ClinicaRepositoryPort;
import br.ucsal.clinica.administracao.porta.SetorRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;

/** Caso de uso ReativarSetor: so e possivel se a clinica do setor estiver ativa. */
public class ReativarSetor {

    private final SetorRepositoryPort repositorio;
    private final ClinicaRepositoryPort clinicas;

    public ReativarSetor(SetorRepositoryPort repositorio, ClinicaRepositoryPort clinicas) {
        this.repositorio = repositorio;
        this.clinicas = clinicas;
    }

    public Setor executar(Long id) {
        Setor setor = repositorio.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Setor", id));
        boolean clinicaAtiva = clinicas.buscarPorId(setor.getClinica().id()).map(c -> c.isAtivo()).orElse(false);
        if (!clinicaAtiva) {
            throw new RegraNegocioException("Não é possível reativar um setor de uma clínica inativa.");
        }
        setor.reativar();
        return repositorio.salvar(setor);
    }
}
