package br.ucsal.clinica.atendimento.adaptador.web;

import java.time.LocalDateTime;
import java.util.List;

import br.ucsal.clinica.atendimento.aplicacao.EmitirPrescricaoMedica;
import br.ucsal.clinica.atendimento.aplicacao.FinalizarAtendimento;
import br.ucsal.clinica.atendimento.aplicacao.RealizarTriagemPaciente;
import br.ucsal.clinica.atendimento.aplicacao.RegistrarEvolucaoClinica;
import br.ucsal.clinica.atendimento.dominio.AtendimentoClinico;
import br.ucsal.clinica.atendimento.dominio.EtapaAtendimento;
import br.ucsal.clinica.atendimento.dominio.OrientacaoEnfermagem;
import br.ucsal.clinica.atendimento.dominio.PrescricaoMedica;
import br.ucsal.clinica.atendimento.dominio.TipoAtendimento;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public final class AtendimentoDTO {

    private AtendimentoDTO() {
    }

    /** Triagem: abre o atendimento. O colaborador responsavel NAO e enviado - vem do login (RN028). */
    public record TriagemRequest(
            @NotNull(message = "Informe o paciente") Long pacienteId,
            Long agendamentoId,
            @NotNull(message = "Informe o tipo: URGENCIA, EMERGENCIA, CONSULTA ou REVISAO") TipoAtendimento tipo,
            @NotBlank(message = "Registre os sintomas apresentados pelo paciente") String sintomas) {

        public RealizarTriagemPaciente.Comando paraComando() {
            return new RealizarTriagemPaciente.Comando(pacienteId, agendamentoId, tipo, sintomas);
        }
    }

    /** RF017 a RF019: sintomas, diagnostico e tratamento. */
    public record EvolucaoRequest(
            @NotBlank(message = "Os sintomas são obrigatórios") String sintomas,
            String diagnostico,
            String tratamento,
            Long versao) {

        public RegistrarEvolucaoClinica.Comando paraComando() {
            return new RegistrarEvolucaoClinica.Comando(sintomas, diagnostico, tratamento, versao);
        }
    }

    public record ItemPrescricaoRequest(
            @NotNull(message = "Informe o medicamento") Long medicamentoId,
            @NotBlank(message = "Informe a dosagem") @Size(max = 100) String dosagem,
            @Size(max = 200) String posologia) {
    }

    /** RF020: medicamentos com as respectivas dosagens. */
    public record PrescricaoRequest(@NotEmpty(message = "Informe ao menos um medicamento") List<@Valid ItemPrescricaoRequest> itens) {

        public List<EmitirPrescricaoMedica.Item> paraItens() {
            return itens.stream()
                    .map(i -> new EmitirPrescricaoMedica.Item(i.medicamentoId(), i.dosagem(), i.posologia())).toList();
        }
    }

    /** RF032: finalizacao do atendimento (o agendamento vira FINALIZADO). */
    public record FinalizacaoRequest(String diagnostico, String tratamento, boolean internado) {

        public FinalizarAtendimento.Comando paraComando() {
            return new FinalizarAtendimento.Comando(diagnostico, tratamento, internado);
        }
    }

    public record PrescricaoResponse(Long id, Long medicamentoId, String medicamento, String dosagem, String posologia) {

        public static PrescricaoResponse de(PrescricaoMedica p) {
            return new PrescricaoResponse(p.id(), p.medicamento().id(), p.medicamento().nome(), p.dosagem(),
                    p.posologia());
        }
    }

    /** RF016: paciente, colaborador, tipo, inicio, sintomas, diagnostico, tratamento, medicamentos, encerramento. */
    public record Response(
            Long id,
            Long prontuarioId,
            String prontuarioNumero,
            Long pacienteId,
            String pacienteNome,
            Long colaboradorId,
            String colaboradorNome,
            Long agendamentoId,
            String convenio,
            TipoAtendimento tipo,
            EtapaAtendimento etapa,
            LocalDateTime dataHoraInicio,
            String sintomas,
            String diagnostico,
            String tratamento,
            List<PrescricaoResponse> medicamentos,
            LocalDateTime dataHoraEncerramento,
            boolean internado,
            Long versao,
            LocalDateTime dataCadastro,
            Status status,
            String motivoInativacao) {

        public static Response de(AtendimentoClinico a) {
            return new Response(a.getId(), Referencia.idDe(a.getProntuario()), Referencia.nomeDe(a.getProntuario()),
                    Referencia.idDe(a.getPaciente()), Referencia.nomeDe(a.getPaciente()),
                    Referencia.idDe(a.getColaborador()), Referencia.nomeDe(a.getColaborador()), a.getAgendamentoId(),
                    a.getConvenio() != null ? a.getConvenio().nome() : "Particular", a.getTipo(), a.getEtapa(),
                    a.getDataHoraInicio(), a.getSintomas(), a.getDiagnostico(), a.getTratamento(),
                    a.getPrescricoes().stream().map(PrescricaoResponse::de).toList(), a.getDataHoraEncerramento(),
                    a.isInternado(), a.getVersao(), a.getDataCadastro(), a.getStatus(), a.getMotivoInativacao());
        }
    }

    public record OrientacaoRequest(
            @NotNull(message = "Informe o atendimento") Long atendimentoId,
            @NotBlank(message = "Escreva a orientação para a enfermagem") String orientacao) {
    }

    public record OrientacaoResponse(Long id, Long atendimentoId, Long colaboradorId, String colaboradorNome,
            String orientacao, LocalDateTime dataHora) {

        public static OrientacaoResponse de(OrientacaoEnfermagem o) {
            return new OrientacaoResponse(o.getId(), o.getAtendimentoId(), Referencia.idDe(o.getColaborador()),
                    Referencia.nomeDe(o.getColaborador()), o.getOrientacao(), o.getDataHora());
        }
    }

    /** RF023 / RN019: medicamento, quantidade e justificativa. O solicitante vem do login (RF038). */
    public record SolicitacaoRequest(
            @NotNull(message = "Informe o medicamento") Long medicamentoId,
            @NotNull @Positive Integer quantidade,
            @NotBlank(message = "Informe a justificativa") String justificativa,
            Long atendimentoId) {
    }
}
