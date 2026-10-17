package br.ucsal.clinica.estoque.adaptador.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.ucsal.clinica.compartilhado.dominio.Status;
import jakarta.persistence.LockModeType;

public interface MedicamentoJpaRepository extends JpaRepository<MedicamentoEntity, Long> {

    Page<MedicamentoEntity> findByStatus(Status status, Pageable pageable);

    boolean existsByNomeIgnoreCaseAndFabricanteIgnoreCase(String nome, String fabricante);

    boolean existsByNomeIgnoreCaseAndFabricanteIgnoreCaseAndIdNot(String nome, String fabricante, Long id);

    /** DA013: a versao do medicamento sobe ao confirmar a transacao (bloqueio otimista do estoque). */
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select m from MedicamentoEntity m where m.id = :id")
    Optional<MedicamentoEntity> buscarParaMovimentar(@Param("id") Long id);

    /** Saldo calculado pela view vw_estoque_saldo (entradas - saidas). */
    @Query(value = "select saldo from vw_estoque_saldo where medicamento_id = :id", nativeQuery = true)
    Long saldo(@Param("id") Long id);

    @Query(value = """
            select medicamento_id as "medicamentoId", nome as "nome", fabricante as "fabricante",
                   estoque_minimo as "estoqueMinimo", saldo as "saldo", status as "status"
              from vw_estoque_saldo
             where status = 'ATIVO'
             order by nome
            """, nativeQuery = true)
    List<LinhaSaldo> saldos();

    /** RN015/RN016: saldo menor ou igual ao estoque minimo (view vw_estoque_reposicao). */
    @Query(value = """
            select medicamento_id as "medicamentoId", nome as "nome", fabricante as "fabricante",
                   estoque_minimo as "estoqueMinimo", saldo as "saldo", status as "status"
              from vw_estoque_reposicao
             order by saldo, nome
            """, nativeQuery = true)
    List<LinhaSaldo> abaixoDoMinimo();

    /** Projecao das consultas de saldo. */
    interface LinhaSaldo {

        Long getMedicamentoId();

        String getNome();

        String getFabricante();

        Integer getEstoqueMinimo();

        Long getSaldo();

        String getStatus();
    }
}
