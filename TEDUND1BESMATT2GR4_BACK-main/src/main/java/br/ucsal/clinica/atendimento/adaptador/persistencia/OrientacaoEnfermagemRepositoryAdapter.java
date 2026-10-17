package br.ucsal.clinica.atendimento.adaptador.persistencia;

import java.util.List;

import org.springframework.stereotype.Component;

import br.ucsal.clinica.administracao.adaptador.persistencia.ColaboradorJpaRepository;
import br.ucsal.clinica.atendimento.dominio.OrientacaoEnfermagem;
import br.ucsal.clinica.atendimento.porta.OrientacaoEnfermagemRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.Referencia;

/** Adaptador JPA da porta das orientacoes de enfermagem. */
@Component
public class OrientacaoEnfermagemRepositoryAdapter implements OrientacaoEnfermagemRepositoryPort {

    private final OrientacaoEnfermagemJpaRepository jpa;
    private final AtendimentoJpaRepository atendimentos;
    private final ColaboradorJpaRepository colaboradores;

    public OrientacaoEnfermagemRepositoryAdapter(OrientacaoEnfermagemJpaRepository jpa,
            AtendimentoJpaRepository atendimentos, ColaboradorJpaRepository colaboradores) {
        this.jpa = jpa;
        this.atendimentos = atendimentos;
        this.colaboradores = colaboradores;
    }

    @Override
    public OrientacaoEnfermagem registrar(OrientacaoEnfermagem o) {
        OrientacaoEnfermagemEntity e = new OrientacaoEnfermagemEntity();
        e.setAtendimento(atendimentos.getReferenceById(o.getAtendimentoId()));
        e.setColaborador(colaboradores.getReferenceById(o.getColaborador().id()));
        e.setOrientacao(o.getOrientacao());
        e.setDataHora(o.getDataHora());
        return paraDominio(jpa.saveAndFlush(e));
    }

    @Override
    public List<OrientacaoEnfermagem> listarDoAtendimento(Long atendimentoId) {
        return jpa.findByAtendimentoIdOrderByDataHora(atendimentoId).stream()
                .map(OrientacaoEnfermagemRepositoryAdapter::paraDominio).toList();
    }

    public static OrientacaoEnfermagem paraDominio(OrientacaoEnfermagemEntity e) {
        OrientacaoEnfermagem o = new OrientacaoEnfermagem();
        o.setId(e.getId());
        o.setAtendimentoId(e.getAtendimento().getId());
        o.setColaborador(new Referencia(e.getColaborador().getId(), e.getColaborador().getNomeCompleto()));
        o.setOrientacao(e.getOrientacao());
        o.setDataHora(e.getDataHora());
        return o;
    }
}
