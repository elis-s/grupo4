package br.ucsal.clinica.estoque.adaptador.persistencia;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.ucsal.clinica.estoque.dominio.TipoMovimentacao;

public interface MovimentacaoEstoqueJpaRepository extends JpaRepository<MovimentacaoEstoqueEntity, Long> {

    /** Filtros opcionais: medicamentoId = 0 significa "todos". */
    @Query("""
            select m from MovimentacaoEstoqueEntity m
             where (:medicamentoId = 0 or m.medicamento.id = :medicamentoId)
               and m.tipo in :tipos
               and m.dataHora >= :inicio and m.dataHora < :fim
            """)
    Page<MovimentacaoEstoqueEntity> filtrar(@Param("medicamentoId") long medicamentoId,
            @Param("tipos") Collection<TipoMovimentacao> tipos, @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim, Pageable pageable);

    List<MovimentacaoEstoqueEntity> findByDataHoraBetweenOrderByDataHoraDesc(LocalDateTime inicio, LocalDateTime fim);

    List<MovimentacaoEstoqueEntity> findByEntreguePorIdOrderByDataHoraDesc(Long usuarioId);
}
