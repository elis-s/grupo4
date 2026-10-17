package br.ucsal.clinica.administracao.adaptador.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ucsal.clinica.compartilhado.dominio.Status;

public interface EspecialidadeJpaRepository extends JpaRepository<EspecialidadeEntity, Long> {

    List<EspecialidadeEntity> findByStatusOrderByNome(Status status);

    boolean existsByNomeIgnoreCase(String nome);
}
