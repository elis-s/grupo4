package br.ucsal.clinica.atendimento.adaptador.persistencia;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.ucsal.clinica.compartilhado.dominio.Status;

public interface AgendamentoJpaRepository extends JpaRepository<AgendamentoEntity, Long> {

    /** Filtros opcionais: medicoId/pacienteId = 0 significa "todos"; periodo [de, ate]. */
    @Query("""
            select a from AgendamentoEntity a
             where (:medicoId = 0 or a.medico.id = :medicoId)
               and (:pacienteId = 0 or a.paciente.id = :pacienteId)
               and a.dataPrevista between :de and :ate
            """)
    Page<AgendamentoEntity> filtrar(@Param("medicoId") long medicoId, @Param("pacienteId") long pacienteId,
            @Param("de") LocalDate de, @Param("ate") LocalDate ate, Pageable pageable);

    List<AgendamentoEntity> findByMedicoIdAndDataPrevistaGreaterThanEqualAndStatusOrderByDataPrevistaAscHoraPrevistaAsc(
            Long medicoId, LocalDate de, Status status);

    List<AgendamentoEntity> findByMedicoIdOrderByDataPrevistaDescHoraPrevistaDesc(Long medicoId);

    boolean existsByMedicoIdAndDataPrevistaAndHoraPrevistaAndStatus(Long medicoId, LocalDate data, LocalTime hora,
            Status status);

    boolean existsByMedicoIdAndDataPrevistaAndHoraPrevistaAndStatusAndIdNot(Long medicoId, LocalDate data,
            LocalTime hora, Status status, Long id);
}
