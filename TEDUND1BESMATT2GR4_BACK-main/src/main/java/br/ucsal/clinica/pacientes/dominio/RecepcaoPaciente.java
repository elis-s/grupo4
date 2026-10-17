package br.ucsal.clinica.pacientes.dominio;

import java.time.LocalDateTime;

/** Resultado da recepcao (RF028): o agendamento do dia, o paciente e o prontuario dele. */
public record RecepcaoPaciente(AgendamentoParaRecepcao agendamento, LocalDateTime dataRecepcao, Paciente paciente,
        ProntuarioEletronico prontuario) {
}
