package br.ucsal.clinica.administracao.adaptador.persistencia;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.ucsal.clinica.compartilhado.dominio.Status;

public interface ConvenioJpaRepository extends JpaRepository<ConvenioEntity, Long> {

    Page<ConvenioEntity> findByStatus(Status status, Pageable pageable);

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndIdNot(String cnpj, Long id);

    boolean existsByRegistroAns(String registroAns);

    boolean existsByRegistroAnsAndIdNot(String registroAns, Long id);
}
