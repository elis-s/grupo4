package br.ucsal.clinica.administracao.aplicacao;

import java.util.List;

import br.ucsal.clinica.administracao.dominio.Especialidade;
import br.ucsal.clinica.administracao.porta.EspecialidadeRepositoryPort;

/** Consulta das especialidades medicas ativas. */
public class ConsultarEspecialidades {

    private final EspecialidadeRepositoryPort repositorio;

    public ConsultarEspecialidades(EspecialidadeRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public List<Especialidade> ativas() {
        return repositorio.listarAtivas();
    }
}
