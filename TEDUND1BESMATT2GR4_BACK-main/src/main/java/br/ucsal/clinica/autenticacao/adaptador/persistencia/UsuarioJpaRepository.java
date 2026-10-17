package br.ucsal.clinica.autenticacao.adaptador.persistencia;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.ucsal.clinica.compartilhado.dominio.Status;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

    /** Carrega o usuario ja com perfis e colaborador (usado no login e no contexto de seguranca). */
    @Query("select u from UsuarioEntity u left join fetch u.perfis join fetch u.colaborador where u.login = :login")
    Optional<UsuarioEntity> buscarPorLogin(@Param("login") String login);

    Page<UsuarioEntity> findByStatus(Status status, Pageable pageable);

    /** Id do usuario pelo login (usado ao gravar a auditoria). */
    @Query("select u.id from UsuarioEntity u where u.login = :login")
    Optional<Long> idPorLogin(@Param("login") String login);

    boolean existsByLogin(String login);

    boolean existsByColaboradorId(Long colaboradorId);

    boolean existsByColaboradorIdAndIdNot(Long colaboradorId, Long id);

    /** RN037: usuario ativo e colaborador vinculado ativo. */
    boolean existsByLoginAndStatusAndColaboradorStatus(String login, Status status, Status colaboradorStatus);

    /** Ultimo login: atualizacao direta, sem mudar a versao do cadastro. */
    @Modifying
    @Query("update UsuarioEntity u set u.ultimoLogin = :agora where u.id = :id")
    int registrarLogin(@Param("id") Long id, @Param("agora") LocalDateTime agora);
}
