package br.ucsal.clinica.pacientes.adaptador.persistencia;

import java.util.Optional;

import org.springframework.stereotype.Component;

import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.pacientes.dominio.ProntuarioEletronico;
import br.ucsal.clinica.pacientes.porta.ProntuarioRepositoryPort;

/** Adaptador JPA: converte ProntuarioEntity <-> ProntuarioEletronico (secao 3.4.2). */
@Component
public class ProntuarioRepositoryAdapter implements ProntuarioRepositoryPort {

    private final ProntuarioJpaRepository jpa;
    private final PacienteJpaRepository pacientes;

    public ProntuarioRepositoryAdapter(ProntuarioJpaRepository jpa, PacienteJpaRepository pacientes) {
        this.jpa = jpa;
        this.pacientes = pacientes;
    }

    @Override
    public Optional<ProntuarioEletronico> buscarPorId(Long id) {
        return jpa.findById(id).map(ProntuarioRepositoryAdapter::paraDominio);
    }

    @Override
    public Optional<ProntuarioEletronico> buscarPorPaciente(Long pacienteId) {
        return jpa.findByPacienteId(pacienteId).map(ProntuarioRepositoryAdapter::paraDominio);
    }

    @Override
    public ProntuarioEletronico salvar(ProntuarioEletronico p) {
        ProntuarioEntity e;
        if (p.isNovo()) {
            e = new ProntuarioEntity();
            e.receberStatusDe(p);
            e.setPaciente(pacientes.getReferenceById(p.getPacienteId()));
        } else {
            e = jpa.findById(p.getId()).orElseThrow(() -> new RecursoNaoEncontradoException("Prontuário", p.getId()));
            e.prepararAlteracao(p);
        }
        e.setNumero(p.getNumero());
        return paraDominio(jpa.saveAndFlush(e));
    }

    static ProntuarioEletronico paraDominio(ProntuarioEntity e) {
        ProntuarioEletronico p = new ProntuarioEletronico();
        e.copiarControlePara(p);
        p.setPacienteId(e.getPaciente().getId());
        p.setNumero(e.getNumero());
        return p;
    }
}
