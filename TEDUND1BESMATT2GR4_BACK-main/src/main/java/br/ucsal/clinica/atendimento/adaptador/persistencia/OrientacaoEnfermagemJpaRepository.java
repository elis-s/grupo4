package br.ucsal.clinica.atendimento.adaptador.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrientacaoEnfermagemJpaRepository extends JpaRepository<OrientacaoEnfermagemEntity, Long> {

    List<OrientacaoEnfermagemEntity> findByAtendimentoIdOrderByDataHora(Long atendimentoId);

    long countByColaboradorId(Long colaboradorId);
}
