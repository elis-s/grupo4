package br.ucsal.clinica.administracao.porta;

import java.util.List;
import java.util.Optional;

import br.ucsal.clinica.administracao.dominio.Especialidade;

/** Porta de persistencia das especialidades medicas. */
public interface EspecialidadeRepositoryPort {

    Optional<Especialidade> buscarPorId(Long id);

    boolean existeNome(String nome);

    Especialidade salvar(Especialidade especialidade);

    List<Especialidade> listarAtivas();
}
