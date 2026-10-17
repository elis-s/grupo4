package br.ucsal.clinica.atendimento.adaptador.persistencia;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import br.ucsal.clinica.administracao.adaptador.persistencia.ColaboradorJpaRepository;
import br.ucsal.clinica.administracao.adaptador.persistencia.ConvenioEntity;
import br.ucsal.clinica.administracao.adaptador.persistencia.ConvenioJpaRepository;
import br.ucsal.clinica.atendimento.dominio.AtendimentoClinico;
import br.ucsal.clinica.atendimento.dominio.PrescricaoMedica;
import br.ucsal.clinica.atendimento.porta.AtendimentoRepositoryPort;
import br.ucsal.clinica.compartilhado.adaptador.persistencia.Paginas;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.estoque.adaptador.persistencia.MedicamentoJpaRepository;
import br.ucsal.clinica.pacientes.adaptador.persistencia.PacienteEntity;
import br.ucsal.clinica.pacientes.adaptador.persistencia.ProntuarioJpaRepository;

/** Adaptador JPA da porta dos atendimentos clinicos. */
@Component
public class AtendimentoRepositoryAdapter implements AtendimentoRepositoryPort {

    private static final Sort ORDEM = Sort.by(Sort.Direction.DESC, "dataHoraInicio");

    private final AtendimentoJpaRepository jpa;
    private final ProntuarioJpaRepository prontuarios;
    private final ColaboradorJpaRepository colaboradores;
    private final AgendamentoJpaRepository agendamentos;
    private final ConvenioJpaRepository convenios;
    private final MedicamentoJpaRepository medicamentos;

    public AtendimentoRepositoryAdapter(AtendimentoJpaRepository jpa, ProntuarioJpaRepository prontuarios,
            ColaboradorJpaRepository colaboradores, AgendamentoJpaRepository agendamentos,
            ConvenioJpaRepository convenios, MedicamentoJpaRepository medicamentos) {
        this.jpa = jpa;
        this.prontuarios = prontuarios;
        this.colaboradores = colaboradores;
        this.agendamentos = agendamentos;
        this.convenios = convenios;
        this.medicamentos = medicamentos;
    }

    @Override
    public Optional<AtendimentoClinico> buscarPorId(Long id) {
        return jpa.findById(id).map(AtendimentoRepositoryAdapter::paraDominio);
    }

    @Override
    public AtendimentoClinico salvar(AtendimentoClinico a) {
        AtendimentoEntity e;
        if (a.isNovo()) {
            e = new AtendimentoEntity();
            e.receberStatusDe(a);
            e.setProntuario(prontuarios.getReferenceById(a.getProntuario().id()));
            e.setMedico(colaboradores.getReferenceById(a.getColaborador().id()));
            e.setAgendamento(a.getAgendamentoId() == null ? null : agendamentos.getReferenceById(a.getAgendamentoId()));
            e.setConvenio(a.getConvenio() == null ? null : convenios.getReferenceById(a.getConvenio().id()));
        } else {
            e = jpa.findById(a.getId()).orElseThrow(() -> new RecursoNaoEncontradoException("Atendimento", a.getId()));
            e.prepararAlteracao(a);
        }
        e.setTipo(a.getTipo());
        e.setEtapa(a.getEtapa());
        e.setDataHoraInicio(a.getDataHoraInicio());
        e.setSintomas(a.getSintomas());
        e.setDiagnostico(a.getDiagnostico());
        e.setTratamento(a.getTratamento());
        e.setDataHoraEncerramento(a.getDataHoraEncerramento());
        e.setInternado(a.isInternado());
        // Prescricoes: somente inclusao (as ja gravadas nao mudam)
        for (PrescricaoMedica p : a.getPrescricoes()) {
            if (p.id() == null) {
                PrescricaoMedicaEntity nova = new PrescricaoMedicaEntity();
                nova.setAtendimento(e);
                nova.setMedicamento(medicamentos.getReferenceById(p.medicamento().id()));
                nova.setDosagem(p.dosagem());
                nova.setPosologia(p.posologia());
                e.getPrescricoes().add(nova);
            }
        }
        return paraDominio(jpa.saveAndFlush(e));
    }

    @Override
    public Pagina<AtendimentoClinico> listar(Paginacao paginacao) {
        return Paginas.converter(jpa.findAll(Paginas.pedido(paginacao, ORDEM)), AtendimentoRepositoryAdapter::paraDominio);
    }

    @Override
    public Pagina<AtendimentoClinico> listarDoColaborador(Long colaboradorId, Paginacao paginacao) {
        return Paginas.converter(jpa.findByMedicoId(colaboradorId, Paginas.pedido(paginacao, ORDEM)),
                AtendimentoRepositoryAdapter::paraDominio);
    }

    @Override
    public boolean existeParaAgendamento(Long agendamentoId) {
        return jpa.existsByAgendamentoId(agendamentoId);
    }

    public static AtendimentoClinico paraDominio(AtendimentoEntity e) {
        AtendimentoClinico a = new AtendimentoClinico();
        e.copiarControlePara(a);
        a.setProntuario(new Referencia(e.getProntuario().getId(), e.getProntuario().getNumero()));
        PacienteEntity paciente = e.getProntuario().getPaciente();
        a.setPaciente(new Referencia(paciente.getId(), paciente.getNomeCompleto()));
        a.setColaborador(new Referencia(e.getMedico().getId(), e.getMedico().getNomeCompleto()));
        a.setAgendamentoId(e.getAgendamento() != null ? e.getAgendamento().getId() : null);
        ConvenioEntity convenio = e.getConvenio();
        a.setConvenio(convenio == null ? null : new Referencia(convenio.getId(), convenio.getNomeFantasia()));
        a.setTipo(e.getTipo());
        a.setEtapa(e.getEtapa());
        a.setDataHoraInicio(e.getDataHoraInicio());
        a.setSintomas(e.getSintomas());
        a.setDiagnostico(e.getDiagnostico());
        a.setTratamento(e.getTratamento());
        a.setDataHoraEncerramento(e.getDataHoraEncerramento());
        a.setInternado(e.isInternado());
        a.setPrescricoes(new ArrayList<>(e.getPrescricoes().stream()
                .map(p -> new PrescricaoMedica(p.getId(),
                        new Referencia(p.getMedicamento().getId(), p.getMedicamento().getNome()),
                        p.getDosagem(), p.getPosologia()))
                .toList()));
        return a;
    }
}
