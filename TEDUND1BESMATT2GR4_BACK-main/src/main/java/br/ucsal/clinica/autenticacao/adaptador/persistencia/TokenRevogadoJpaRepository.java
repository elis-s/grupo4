package br.ucsal.clinica.autenticacao.adaptador.persistencia;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TokenRevogadoJpaRepository extends JpaRepository<TokenRevogadoEntity, Long> {

    boolean existsByJti(String jti);

    @Modifying
    @Query("delete from TokenRevogadoEntity t where t.dataExpiracao < :agora")
    int removerExpirados(@Param("agora") LocalDateTime agora);
}
