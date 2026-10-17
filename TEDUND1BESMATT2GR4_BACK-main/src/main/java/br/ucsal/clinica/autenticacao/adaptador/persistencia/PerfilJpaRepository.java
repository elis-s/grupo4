package br.ucsal.clinica.autenticacao.adaptador.persistencia;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ucsal.clinica.autenticacao.dominio.NomePerfil;

public interface PerfilJpaRepository extends JpaRepository<PerfilEntity, Long> {

    List<PerfilEntity> findByNomeIn(Collection<NomePerfil> nomes);

    List<PerfilEntity> findAllByOrderByNome();
}
