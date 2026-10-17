package br.ucsal.clinica.administracao.adaptador.persistencia;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.ucsal.clinica.compartilhado.dominio.Status;

public interface ClinicaJpaRepository extends JpaRepository<ClinicaEntity, Long> {

    Page<ClinicaEntity> findByStatus(Status status, Pageable pageable);

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndIdNot(String cnpj, Long id);
}
