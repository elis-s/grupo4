package br.ucsal.clinica.estoque.adaptador.web;

import java.time.LocalDateTime;

import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.Status;
import br.ucsal.clinica.estoque.dominio.SituacaoSolicitacao;
import br.ucsal.clinica.estoque.dominio.SolicitacaoMedicamento;
import jakarta.validation.constraints.NotBlank;

public final class SolicitacaoMedicamentoDTO {

    private SolicitacaoMedicamentoDTO() {
    }

    /** RF059: reprovacao exige o motivo. */
    public record ReprovacaoRequest(@NotBlank(message = "Informe o motivo da reprovação.") String motivo) {
    }

    /** RN019: medicamento, quantidade, justificativa, data de cadastro e status. */
    public record Response(
            Long id,
            Long medicamentoId,
            String medicamento,
            int quantidade,
            String justificativa,
            Long colaboradorSolicitanteId,
            String colaboradorSolicitante,
            Long atendimentoId,
            SituacaoSolicitacao situacao,
            String analisadoPor,
            LocalDateTime dataAnalise,
            String motivoReprovacao,
            Long versao,
            LocalDateTime dataCadastro,
            Status status) {

        public static Response de(SolicitacaoMedicamento s) {
            return new Response(s.getId(), Referencia.idDe(s.getMedicamento()), Referencia.nomeDe(s.getMedicamento()),
                    s.getQuantidade(), s.getJustificativa(), Referencia.idDe(s.getSolicitante()),
                    Referencia.nomeDe(s.getSolicitante()), s.getAtendimentoId(), s.getSituacao(),
                    Referencia.nomeDe(s.getAnalisadoPor()), s.getDataAnalise(), s.getMotivoReprovacao(),
                    s.getVersao(), s.getDataCadastro(), s.getStatus());
        }
    }
}
