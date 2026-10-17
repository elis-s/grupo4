package br.ucsal.clinica.administracao.adaptador.persistencia;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import br.ucsal.clinica.administracao.dominio.Setor;
import br.ucsal.clinica.administracao.porta.SetorRepositoryPort;
import br.ucsal.clinica.compartilhado.adaptador.persistencia.Paginas;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.Status;

/** Adaptador JPA da porta de persistencia dos setores. */
@Component
public class SetorRepositoryAdapter implements SetorRepositoryPort {

    private static final Sort ORDEM = Sort.by("nome");

    private final SetorJpaRepository jpa;
    private final ClinicaJpaRepository clinicas;
    private final ColaboradorJpaRepository colaboradores;

    public SetorRepositoryAdapter(SetorJpaRepository jpa, ClinicaJpaRepository clinicas,
            ColaboradorJpaRepository colaboradores) {
        this.jpa = jpa;
        this.clinicas = clinicas;
        this.colaboradores = colaboradores;
    }

    @Override
    public Optional<Setor> buscarPorId(Long id) {
        return jpa.findById(id).map(SetorRepositoryAdapter::paraDominio);
    }

    @Override
    public Setor salvar(Setor setor) {
        SetorEntity entidade;
        if (setor.isNovo()) {
            entidade = new SetorEntity();
            entidade.receberStatusDe(setor);
        } else {
            entidade = jpa.findById(setor.getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Setor", setor.getId()));
            entidade.prepararAlteracao(setor);
        }
        entidade.setClinica(clinicas.getReferenceById(setor.getClinica().id()));
        entidade.setNome(setor.getNome());
        entidade.setEmail(setor.getEmail());
        entidade.setTelefone(setor.getTelefone());
        entidade.setResponsavel(setor.getResponsavel() == null ? null
                : colaboradores.getReferenceById(setor.getResponsavel().id()));
        return paraDominio(jpa.saveAndFlush(entidade));
    }

    @Override
    public Pagina<Setor> listar(Status status, Paginacao paginacao) {
        PageRequest pedido = Paginas.pedido(paginacao, ORDEM);
        return Paginas.converter(status == null ? jpa.findAll(pedido) : jpa.findByStatus(status, pedido),
                SetorRepositoryAdapter::paraDominio);
    }

    @Override
    public boolean existeNomeNaClinica(Long clinicaId, String nome, Long ignorarId) {
        return ignorarId == null
                ? jpa.existsByClinicaIdAndNomeIgnoreCase(clinicaId, nome)
                : jpa.existsByClinicaIdAndNomeIgnoreCaseAndIdNot(clinicaId, nome, ignorarId);
    }

    static Setor paraDominio(SetorEntity e) {
        Setor s = new Setor();
        e.copiarControlePara(s);
        s.setClinica(new Referencia(e.getClinica().getId(), e.getClinica().getNomeFantasia()));
        s.setNome(e.getNome());
        s.setEmail(e.getEmail());
        s.setTelefone(e.getTelefone());
        ColaboradorEntity r = e.getResponsavel();
        s.setResponsavel(r == null ? null : new Referencia(r.getId(), r.getNomeCompleto()));
        return s;
    }
}
