package br.ucsal.clinica.administracao.aplicacao;

import br.ucsal.clinica.administracao.dominio.Setor;
import br.ucsal.clinica.administracao.porta.ClinicaRepositoryPort;
import br.ucsal.clinica.administracao.porta.ColaboradorRepositoryPort;
import br.ucsal.clinica.administracao.porta.SetorRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;

/** Caso de uso EditarSetor (RF005, RN004, RN005). */
public class EditarSetor {

    private final SetorRepositoryPort repositorio;
    private final ClinicaRepositoryPort clinicas;
    private final ColaboradorRepositoryPort colaboradores;

    public EditarSetor(SetorRepositoryPort repositorio, ClinicaRepositoryPort clinicas,
            ColaboradorRepositoryPort colaboradores) {
        this.repositorio = repositorio;
        this.clinicas = clinicas;
        this.colaboradores = colaboradores;
    }

    public Setor executar(Long id, Setor dados, Long versaoLida) {
        Setor setor = repositorio.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Setor", id));
        setor.verificarVersao(versaoLida);
        CadastrarSetor.prepararReferencias(dados, clinicas, colaboradores);
        if (repositorio.existeNomeNaClinica(dados.getClinica().id(), dados.getNome(), id)) {
            throw new RegraNegocioException("Já existe outro setor com este nome nesta clínica.");
        }
        setor.atualizarCom(dados);
        return repositorio.salvar(setor);
    }
}
