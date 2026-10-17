package br.ucsal.clinica.atendimento.adaptador.persistencia;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AtendimentoJpaRepository extends JpaRepository<AtendimentoEntity, Long> {

    Page<AtendimentoEntity> findByMedicoId(Long colaboradorId, Pageable pageable);

    List<AtendimentoEntity> findByMedicoIdOrderByDataHoraInicioDesc(Long colaboradorId);

    List<AtendimentoEntity> findByProntuarioIdOrderByDataHoraInicioDesc(Long prontuarioId);

    boolean existsByAgendamentoId(Long agendamentoId);
}
