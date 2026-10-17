package br.ucsal.clinica.atendimento.porta;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import br.ucsal.clinica.atendimento.dominio.Agendamento;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;

/** Porta de persistencia dos agendamentos. */
public interface AgendamentoRepositoryPort {

    Optional<Agendamento> buscarPorId(Long id);

    Agendamento salvar(Agendamento agendamento);

    /** RF026: consulta com filtros opcionais (data, medico, paciente). */
    Pagina<Agendamento> listar(LocalDate data, Long medicoId, Long pacienteId, Paginacao paginacao);

    /** Agenda (ativa) do medico a partir de uma data. */
    List<Agendamento> agendaDoMedico(Long medicoId, LocalDate aPartirDe);

    boolean horarioOcupado(Long medicoId, LocalDate data, LocalTime hora, Long ignorarId);

    /**
     * Secao 3.2.4: bloqueio pessimista pontual na agenda do medico, para que dois atendentes
     * nao ocupem o mesmo horario ao mesmo tempo (alocacao dupla).
     */
    void bloquearAgendaDoMedico(Long medicoId);
}
