package br.ucsal.clinica.pacientes.adaptador.persistencia;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.ucsal.clinica.compartilhado.dominio.Status;

public interface PacienteJpaRepository extends JpaRepository<PacienteEntity, Long> {

    Page<PacienteEntity> findByStatus(Status status, Pageable pageable);

    Page<PacienteEntity> findByNomeCompletoContainingIgnoreCase(String nome, Pageable pageable);

    Page<PacienteEntity> findByNomeCompletoContainingIgnoreCaseAndStatus(String nome, Status status, Pageable pageable);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);
}
