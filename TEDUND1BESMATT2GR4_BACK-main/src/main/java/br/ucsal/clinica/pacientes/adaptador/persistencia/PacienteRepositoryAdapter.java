package br.ucsal.clinica.pacientes.adaptador.persistencia;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import br.ucsal.clinica.administracao.adaptador.persistencia.ColaboradorEntity;
import br.ucsal.clinica.administracao.adaptador.persistencia.ColaboradorJpaRepository;
import br.ucsal.clinica.administracao.adaptador.persistencia.ConvenioEntity;
import br.ucsal.clinica.administracao.adaptador.persistencia.ConvenioJpaRepository;
import br.ucsal.clinica.administracao.adaptador.persistencia.EspecialidadeEntity;
import br.ucsal.clinica.administracao.adaptador.persistencia.EspecialidadeJpaRepository;
import br.ucsal.clinica.compartilhado.adaptador.persistencia.Paginas;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.Status;
import br.ucsal.clinica.compartilhado.dominio.Textos;
import br.ucsal.clinica.pacientes.dominio.Paciente;
import br.ucsal.clinica.pacientes.porta.PacienteRepositoryPort;

/** Adaptador JPA da porta dos pacientes. */
@Component
public class PacienteRepositoryAdapter implements PacienteRepositoryPort {

    private static final Sort ORDEM = Sort.by("nomeCompleto");

    private final PacienteJpaRepository jpa;
    private final ProntuarioJpaRepository prontuarios;
    private final ColaboradorJpaRepository colaboradores;
    private final EspecialidadeJpaRepository especialidades;
    private final ConvenioJpaRepository convenios;

    public PacienteRepositoryAdapter(PacienteJpaRepository jpa, ProntuarioJpaRepository prontuarios,
            ColaboradorJpaRepository colaboradores, EspecialidadeJpaRepository especialidades,
            ConvenioJpaRepository convenios) {
        this.jpa = jpa;
        this.prontuarios = prontuarios;
        this.colaboradores = colaboradores;
        this.especialidades = especialidades;
        this.convenios = convenios;
    }

    @Override
    public Optional<Paciente> buscarPorId(Long id) {
        return jpa.findById(id).map(this::paraDominio);
    }

    @Override
    public Paciente salvar(Paciente p) {
        PacienteEntity e;
        if (p.isNovo()) {
            e = new PacienteEntity();
            e.receberStatusDe(p);
        } else {
            e = jpa.findById(p.getId()).orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", p.getId()));
            e.prepararAlteracao(p);
        }
        e.setNomeCompleto(p.getNomeCompleto());
        e.setCpf(p.getCpf());
        e.setDataNascimento(p.getDataNascimento());
        e.setSexo(p.getSexo());
        e.setTelefone(p.getTelefone());
        e.setEmail(p.getEmail());
        e.setMedico(p.getMedico() == null ? null : colaboradores.getReferenceById(p.getMedico().id()));
        e.setEspecialidade(p.getEspecialidade() == null ? null
                : especialidades.getReferenceById(p.getEspecialidade().id()));
        e.setConvenio(p.getConvenio() == null ? null : convenios.getReferenceById(p.getConvenio().id()));
        e.setNumeroCarteirinha(p.getNumeroCarteirinha());
        return paraDominio(jpa.saveAndFlush(e));
    }

    @Override
    public Pagina<Paciente> listar(Status status, String nome, Paginacao paginacao) {
        PageRequest pedido = Paginas.pedido(paginacao, ORDEM);
        String filtro = Textos.vazioParaNulo(nome);
        Page<PacienteEntity> pagina;
        if (filtro != null && status != null) {
            pagina = jpa.findByNomeCompletoContainingIgnoreCaseAndStatus(filtro, status, pedido);
        } else if (filtro != null) {
            pagina = jpa.findByNomeCompletoContainingIgnoreCase(filtro, pedido);
        } else if (status != null) {
            pagina = jpa.findByStatus(status, pedido);
        } else {
            pagina = jpa.findAll(pedido);
        }
        return Paginas.converter(pagina, this::paraDominio);
    }

    @Override
    public boolean existeCpf(String cpf, Long ignorarId) {
        return ignorarId == null ? jpa.existsByCpf(cpf) : jpa.existsByCpfAndIdNot(cpf, ignorarId);
    }

    private Paciente paraDominio(PacienteEntity e) {
        Paciente p = new Paciente();
        e.copiarControlePara(p);
        p.setNomeCompleto(e.getNomeCompleto());
        p.setCpf(e.getCpf());
        p.setDataNascimento(e.getDataNascimento());
        p.setSexo(e.getSexo());
        p.setTelefone(e.getTelefone());
        p.setEmail(e.getEmail());
        ColaboradorEntity medico = e.getMedico();
        p.setMedico(medico == null ? null : new Referencia(medico.getId(), medico.getNomeCompleto()));
        EspecialidadeEntity especialidade = e.getEspecialidade();
        p.setEspecialidade(especialidade == null ? null : new Referencia(especialidade.getId(), especialidade.getNome()));
        ConvenioEntity convenio = e.getConvenio();
        p.setConvenio(convenio == null ? null : new Referencia(convenio.getId(), convenio.getNomeFantasia()));
        p.setNumeroCarteirinha(e.getNumeroCarteirinha());
        if (e.getId() != null) {
            prontuarios.findByPacienteId(e.getId())
                    .ifPresent(pr -> p.setProntuario(new Referencia(pr.getId(), pr.getNumero())));
        }
        return p;
    }
}
