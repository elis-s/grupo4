package br.ucsal.clinica.atendimento.adaptador.persistencia;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import br.ucsal.clinica.administracao.adaptador.persistencia.ColaboradorJpaRepository;
import br.ucsal.clinica.atendimento.dominio.Agendamento;
import br.ucsal.clinica.atendimento.porta.AgendamentoRepositoryPort;
import br.ucsal.clinica.compartilhado.adaptador.persistencia.Paginas;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.Status;
import br.ucsal.clinica.pacientes.adaptador.persistencia.PacienteJpaRepository;

/** Adaptador JPA da porta dos agendamentos. */
@Component
public class AgendamentoRepositoryAdapter implements AgendamentoRepositoryPort {

    private static final Sort ORDEM = Sort.by("dataPrevista", "horaPrevista");
    private static final LocalDate INICIO_PADRAO = LocalDate.of(1900, 1, 1);
    private static final LocalDate FIM_PADRAO = LocalDate.of(3000, 1, 1);

    private final AgendamentoJpaRepository jpa;
    private final ColaboradorJpaRepository colaboradores;
    private final PacienteJpaRepository pacientes;

    public AgendamentoRepositoryAdapter(AgendamentoJpaRepository jpa, ColaboradorJpaRepository colaboradores,
            PacienteJpaRepository pacientes) {
        this.jpa = jpa;
        this.colaboradores = colaboradores;
        this.pacientes = pacientes;
    }

    @Override
    public Optional<Agendamento> buscarPorId(Long id) {
        return jpa.findById(id).map(AgendamentoRepositoryAdapter::paraDominio);
    }

    @Override
    public Agendamento salvar(Agendamento a) {
        AgendamentoEntity e;
        if (a.isNovo()) {
            e = new AgendamentoEntity();
            e.receberStatusDe(a);
        } else {
            e = jpa.findById(a.getId()).orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento", a.getId()));
            e.prepararAlteracao(a);
        }
        e.setMedico(colaboradores.getReferenceById(a.getMedico().id()));
        e.setPaciente(pacientes.getReferenceById(a.getPaciente().id()));
        e.setDataPrevista(a.getDataPrevista());
        e.setHoraPrevista(a.getHoraPrevista());
        e.setSituacao(a.getSituacao());
        e.setObservacao(a.getObservacao());
        e.setDataRecepcao(a.getDataRecepcao());
        return paraDominio(jpa.saveAndFlush(e));
    }

    @Override
    public Pagina<Agendamento> listar(LocalDate data, Long medicoId, Long pacienteId, Paginacao paginacao) {
        return Paginas.converter(jpa.filtrar(medicoId == null ? 0L : medicoId, pacienteId == null ? 0L : pacienteId,
                data == null ? INICIO_PADRAO : data, data == null ? FIM_PADRAO : data,
                Paginas.pedido(paginacao, ORDEM)), AgendamentoRepositoryAdapter::paraDominio);
    }

    @Override
    public List<Agendamento> agendaDoMedico(Long medicoId, LocalDate aPartirDe) {
        return jpa.findByMedicoIdAndDataPrevistaGreaterThanEqualAndStatusOrderByDataPrevistaAscHoraPrevistaAsc(
                medicoId, aPartirDe, Status.ATIVO).stream().map(AgendamentoRepositoryAdapter::paraDominio).toList();
    }

    @Override
    public boolean horarioOcupado(Long medicoId, LocalDate data, LocalTime hora, Long ignorarId) {
        return ignorarId == null
                ? jpa.existsByMedicoIdAndDataPrevistaAndHoraPrevistaAndStatus(medicoId, data, hora, Status.ATIVO)
                : jpa.existsByMedicoIdAndDataPrevistaAndHoraPrevistaAndStatusAndIdNot(medicoId, data, hora,
                        Status.ATIVO, ignorarId);
    }

    @Override
    public void bloquearAgendaDoMedico(Long medicoId) {
        colaboradores.bloquearParaAgendamento(medicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador", medicoId));
    }

    public static Agendamento paraDominio(AgendamentoEntity e) {
        Agendamento a = new Agendamento();
        e.copiarControlePara(a);
        a.setMedico(new Referencia(e.getMedico().getId(), e.getMedico().getNomeCompleto()));
        a.setPaciente(new Referencia(e.getPaciente().getId(), e.getPaciente().getNomeCompleto()));
        a.setDataPrevista(e.getDataPrevista());
        a.setHoraPrevista(e.getHoraPrevista());
        a.setSituacao(e.getSituacao());
        a.setObservacao(e.getObservacao());
        a.setDataRecepcao(e.getDataRecepcao());
        return a;
    }
}
