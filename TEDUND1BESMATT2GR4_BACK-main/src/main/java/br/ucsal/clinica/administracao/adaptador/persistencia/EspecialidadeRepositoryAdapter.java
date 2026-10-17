package br.ucsal.clinica.administracao.adaptador.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import br.ucsal.clinica.administracao.dominio.Especialidade;
import br.ucsal.clinica.administracao.porta.EspecialidadeRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.Status;

/** Adaptador JPA da porta das especialidades medicas. */
@Component
public class EspecialidadeRepositoryAdapter implements EspecialidadeRepositoryPort {

    private final EspecialidadeJpaRepository jpa;

    public EspecialidadeRepositoryAdapter(EspecialidadeJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Especialidade> buscarPorId(Long id) {
        return jpa.findById(id).map(EspecialidadeRepositoryAdapter::paraDominio);
    }

    @Override
    public boolean existeNome(String nome) {
        return jpa.existsByNomeIgnoreCase(nome);
    }

    @Override
    public Especialidade salvar(Especialidade especialidade) {
        EspecialidadeEntity e = especialidade.id() == null ? new EspecialidadeEntity()
                : jpa.findById(especialidade.id()).orElseGet(EspecialidadeEntity::new);
        e.setNome(especialidade.nome());
        e.setStatus(especialidade.status() == null ? Status.ATIVO : especialidade.status());
        return paraDominio(jpa.saveAndFlush(e));
    }

    @Override
    public List<Especialidade> listarAtivas() {
        return jpa.findByStatusOrderByNome(Status.ATIVO).stream()
                .map(EspecialidadeRepositoryAdapter::paraDominio).toList();
    }

    static Especialidade paraDominio(EspecialidadeEntity e) {
        return new Especialidade(e.getId(), e.getNome(), e.getStatus());
    }
}
