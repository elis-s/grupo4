package br.ucsal.clinica.pacientes.adaptador.persistencia;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Component;

import br.ucsal.clinica.atendimento.adaptador.persistencia.AgendamentoEntity;
import br.ucsal.clinica.atendimento.adaptador.persistencia.AgendamentoJpaRepository;
import br.ucsal.clinica.atendimento.dominio.SituacaoAgendamento;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Status;
import br.ucsal.clinica.pacientes.dominio.AgendamentoParaRecepcao;
import br.ucsal.clinica.pacientes.porta.AgendamentoRecepcaoPort;

/** Implementa a porta da recepcao (RF028) lendo e marcando a chegada no agendamento. */
@Component
public class AgendamentoRecepcaoAdapter implements AgendamentoRecepcaoPort {

    private final AgendamentoJpaRepository agendamentos;

    public AgendamentoRecepcaoAdapter(AgendamentoJpaRepository agendamentos) {
        this.agendamentos = agendamentos;
    }

    @Override
    public Optional<AgendamentoParaRecepcao> buscar(Long agendamentoId) {
        return agendamentos.findById(agendamentoId).map(a -> new AgendamentoParaRecepcao(a.getId(),
                a.getPaciente().getId(), a.getMedico().getNomeCompleto(), a.getDataPrevista(), a.getHoraPrevista(),
                a.getStatus() == Status.ATIVO, a.getSituacao() == SituacaoAgendamento.FINALIZADO, a.getDataRecepcao(),
                a.getMotivoInativacao()));
    }

    @Override
    public void registrarChegada(Long agendamentoId, LocalDateTime quando) {
        AgendamentoEntity a = agendamentos.findById(agendamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento", agendamentoId));
        a.capturarEstadoAnterior();
        a.setDataRecepcao(quando);
        agendamentos.saveAndFlush(a);
    }
}
