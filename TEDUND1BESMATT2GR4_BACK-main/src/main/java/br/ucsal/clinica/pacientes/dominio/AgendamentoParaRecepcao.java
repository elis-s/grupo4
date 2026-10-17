package br.ucsal.clinica.pacientes.dominio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Dados do agendamento que a recepcao precisa (RF028), lidos do modulo de Agendamento. */
public record AgendamentoParaRecepcao(
        Long id,
        Long pacienteId,
        String medico,
        LocalDate dataPrevista,
        LocalTime horaPrevista,
        boolean ativo,
        boolean finalizado,
        LocalDateTime dataRecepcao,
        String motivoInativacao) {
}
