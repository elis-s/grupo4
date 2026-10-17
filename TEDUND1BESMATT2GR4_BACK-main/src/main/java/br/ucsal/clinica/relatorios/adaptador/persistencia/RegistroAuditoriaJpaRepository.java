package br.ucsal.clinica.relatorios.adaptador.persistencia;

import java.time.LocalDateTime;
import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.ucsal.clinica.relatorios.dominio.AcaoAuditoria;

public interface RegistroAuditoriaJpaRepository extends JpaRepository<RegistroAuditoriaEntity, Long> {

    /** Filtros opcionais: texto vazio = todos. */
    @Query("""
            select a from RegistroAuditoriaEntity a
             where (:login = '' or a.login = :login)
               and (:entidade = '' or a.entidade = :entidade)
               and a.acao in :acoes
               and a.dataHora >= :inicio and a.dataHora < :fim
            """)
    Page<RegistroAuditoriaEntity> filtrar(@Param("login") String login, @Param("entidade") String entidade,
            @Param("acoes") Collection<AcaoAuditoria> acoes, @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim, Pageable pageable);
}
