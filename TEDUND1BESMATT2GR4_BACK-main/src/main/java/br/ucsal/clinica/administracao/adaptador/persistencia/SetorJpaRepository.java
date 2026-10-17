package br.ucsal.clinica.administracao.adaptador.persistencia;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.ucsal.clinica.compartilhado.dominio.Status;

public interface SetorJpaRepository extends JpaRepository<SetorEntity, Long> {

    Page<SetorEntity> findByStatus(Status status, Pageable pageable);

    boolean existsByClinicaIdAndNomeIgnoreCase(Long clinicaId, String nome);

    boolean existsByClinicaIdAndNomeIgnoreCaseAndIdNot(Long clinicaId, String nome, Long id);
}
