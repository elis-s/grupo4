package br.ucsal.clinica.pacientes.adaptador.web;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import br.ucsal.clinica.pacientes.dominio.RecepcaoPaciente;
import jakarta.validation.constraints.NotNull;

public final class RecepcaoDTO {

    private RecepcaoDTO() {
    }

    public record Request(@NotNull(message = "Informe o agendamento") Long agendamentoId) {
    }

    /** RF028: dados do agendamento, do paciente e do prontuario gerado no cadastro. */
    public record Response(
            Long agendamentoId,
            LocalDate dataPrevista,
            LocalTime horaPrevista,
            String medico,
            LocalDateTime dataRecepcao,
            PacienteDTO.Response paciente,
            Long prontuarioId,
            String prontuarioNumero) {

        public static Response de(RecepcaoPaciente r) {
            return new Response(r.agendamento().id(), r.agendamento().dataPrevista(), r.agendamento().horaPrevista(),
                    r.agendamento().medico(), r.dataRecepcao(), PacienteDTO.Response.de(r.paciente()),
                    r.prontuario().getId(), r.prontuario().getNumero());
        }
    }
}
