package br.ucsal.clinica.administracao.aplicacao;

import br.ucsal.clinica.administracao.dominio.Clinica;
import br.ucsal.clinica.administracao.dominio.Colaborador;
import br.ucsal.clinica.administracao.dominio.Setor;
import br.ucsal.clinica.administracao.porta.ClinicaRepositoryPort;
import br.ucsal.clinica.administracao.porta.ColaboradorRepositoryPort;
import br.ucsal.clinica.administracao.porta.SetorRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;

/** Caso de uso CadastrarSetor (RF004, RN004, RN005). */
public class CadastrarSetor {

    private final SetorRepositoryPort repositorio;
    private final ClinicaRepositoryPort clinicas;
    private final ColaboradorRepositoryPort colaboradores;

    public CadastrarSetor(SetorRepositoryPort repositorio, ClinicaRepositoryPort clinicas,
            ColaboradorRepositoryPort colaboradores) {
        this.repositorio = repositorio;
        this.clinicas = clinicas;
        this.colaboradores = colaboradores;
    }

    public Setor executar(Setor novo) {
        prepararReferencias(novo, clinicas, colaboradores);
        if (repositorio.existeNomeNaClinica(novo.getClinica().id(), novo.getNome(), null)) {
            throw new RegraNegocioException("Já existe um setor com este nome nesta clínica.");
        }
        return repositorio.salvar(novo);
    }

    /** Valida o setor e confirma que a clinica e o responsavel existem e estao ativos. */
    static void prepararReferencias(Setor setor, ClinicaRepositoryPort clinicas, ColaboradorRepositoryPort colaboradores) {
        setor.validar();
        Long clinicaId = setor.getClinica().id();
        Clinica clinica = clinicas.buscarPorId(clinicaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Clínica", clinicaId));
        if (!clinica.isAtivo()) {
            throw new RegraNegocioException("A clínica informada está inativa.");
        }
        Long responsavelId = setor.getResponsavel().id();
        Colaborador responsavel = colaboradores.buscarPorId(responsavelId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador", responsavelId));
        if (!responsavel.isAtivo()) {
            throw new RegraNegocioException("O responsável informado está inativo.");
        }
        setor.setClinica(new Referencia(clinica.getId(), clinica.getNomeFantasia()));
        setor.setResponsavel(new Referencia(responsavel.getId(), responsavel.getNomeCompleto()));
    }
}
