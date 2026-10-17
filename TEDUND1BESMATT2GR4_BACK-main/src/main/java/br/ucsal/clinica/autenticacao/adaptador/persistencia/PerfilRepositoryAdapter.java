package br.ucsal.clinica.autenticacao.adaptador.persistencia;

import java.util.List;

import org.springframework.stereotype.Component;

import br.ucsal.clinica.autenticacao.dominio.Perfil;
import br.ucsal.clinica.autenticacao.porta.PerfilRepositoryPort;

/** Adaptador JPA da porta dos perfis de acesso. */
@Component
public class PerfilRepositoryAdapter implements PerfilRepositoryPort {

    private final PerfilJpaRepository jpa;

    public PerfilRepositoryAdapter(PerfilJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Perfil> listar() {
        return jpa.findAllByOrderByNome().stream()
                .map(p -> new Perfil(p.getId(), p.getNome(), p.getDescricao())).toList();
    }
}
