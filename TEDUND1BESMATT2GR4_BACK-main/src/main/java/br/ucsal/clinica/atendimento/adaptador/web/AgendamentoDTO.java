package br.ucsal.clinica.atendimento.adaptador.web;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import br.ucsal.clinica.atendimento.dominio.Agendamento;
import br.ucsal.clinica.atendimento.dominio.SituacaoAgendamento;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.Status;
import jakarta.validation.constraints.NotNull;

public final class AgendamentoDTO {

    private AgendamentoDTO() {
    }

    /** RF025: colaborador (medico), paciente, data e hora agendadas. */
    public record Request(
            @NotNull(message = "Informe o médico") Long medicoId,
            @NotNull(message = "Informe o paciente") Long pacienteId,
            @NotNull(message = "Informe a data da consulta") LocalDate dataPrevista,
            @NotNull(message = "Informe a hora da consulta") LocalTime horaPrevista,
            String observacao,
            Long versao) {

        public Agendamento paraDominio() {
            Agendamento a = new Agendamento();
            a.setMedico(Referencia.de(medicoId));
            a.setPaciente(Referencia.de(pacienteId));
            a.setDataPrevista(dataPrevista);
            a.setHoraPrevista(horaPrevista);
            a.setObservacao(observacao);
            return a;
        }
    }

    /** RF026 / RF032: dados da consulta agendada e o status (AGENDADO, FINALIZADO ou INATIVO). */
    public record Response(
            Long id,
            Long medicoId,
            String medicoNome,
            Long pacienteId,
            String pacienteNome,
            LocalDate dataPrevista,
            LocalTime horaPrevista,
            SituacaoAgendamento situacao,
            String observacao,
            LocalDateTime dataRecepcao,
            Long versao,
            LocalDateTime dataCadastro,
            Status status,
            String motivoInativacao,
            LocalDateTime dataInativacao) {

        public static Response de(Agendamento a) {
            return new Response(a.getId(), Referencia.idDe(a.getMedico()), Referencia.nomeDe(a.getMedico()),
                    Referencia.idDe(a.getPaciente()), Referencia.nomeDe(a.getPaciente()), a.getDataPrevista(),
                    a.getHoraPrevista(), a.getSituacao(), a.getObservacao(), a.getDataRecepcao(), a.getVersao(),
                    a.getDataCadastro(), a.getStatus(), a.getMotivoInativacao(), a.getDataInativacao());
        }
    }
}
