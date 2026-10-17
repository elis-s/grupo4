package br.ucsal.clinica.administracao.adaptador.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.ucsal.clinica.administracao.dominio.Funcao;
import br.ucsal.clinica.compartilhado.dominio.Status;
import jakarta.persistence.LockModeType;

public interface ColaboradorJpaRepository extends JpaRepository<ColaboradorEntity, Long> {

    Page<ColaboradorEntity> findByStatus(Status status, Pageable pageable);

    List<ColaboradorEntity> findByFuncaoAndStatusOrderByNomeCompleto(Funcao funcao, Status status);

    /**
     * Secao 3.2.4: bloqueio pessimista pontual (SELECT ... FOR UPDATE) na agenda do medico,
     * para evitar dois agendamentos simultaneos no mesmo horario.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ColaboradorEntity c where c.id = :id")
    Optional<ColaboradorEntity> bloquearParaAgendamento(@Param("id") Long id);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    boolean existsByConselhoSiglaAndConselhoUfAndConselhoNumero(String sigla, String uf, String numero);

    boolean existsByConselhoSiglaAndConselhoUfAndConselhoNumeroAndIdNot(String sigla, String uf, String numero,
            Long id);
}
