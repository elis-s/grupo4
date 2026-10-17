package br.ucsal.clinica.administracao.aplicacao;

import br.ucsal.clinica.administracao.dominio.Especialidade;
import br.ucsal.clinica.administracao.porta.EspecialidadeRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.compartilhado.dominio.Status;
import br.ucsal.clinica.compartilhado.dominio.Textos;

/** Caso de uso MapearEspecialidadeMedica (secao 3.2.2): cadastra uma especialidade medica. */
public class MapearEspecialidadeMedica {

    private final EspecialidadeRepositoryPort repositorio;

    public MapearEspecialidadeMedica(EspecialidadeRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Especialidade executar(String nome) {
        String nomeLimpo = Textos.obrigatorio(nome, "Informe o nome da especialidade.");
        if (repositorio.existeNome(nomeLimpo)) {
            throw new RegraNegocioException("Especialidade já cadastrada.");
        }
        return repositorio.salvar(new Especialidade(null, nomeLimpo, Status.ATIVO));
    }
}
