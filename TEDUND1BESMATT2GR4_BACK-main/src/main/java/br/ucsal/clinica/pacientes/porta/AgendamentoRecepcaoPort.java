package br.ucsal.clinica.pacientes.porta;

import java.time.LocalDateTime;
import java.util.Optional;

import br.ucsal.clinica.pacientes.dominio.AgendamentoParaRecepcao;

/** O que a recepcao do paciente (RF028) precisa do agendamento, sem depender do modulo de Agendamento. */
public interface AgendamentoRecepcaoPort {

    Optional<AgendamentoParaRecepcao> buscar(Long agendamentoId);

    void registrarChegada(Long agendamentoId, LocalDateTime quando);
}
