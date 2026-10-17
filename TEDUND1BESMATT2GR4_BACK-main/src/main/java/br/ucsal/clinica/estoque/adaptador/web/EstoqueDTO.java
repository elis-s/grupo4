package br.ucsal.clinica.estoque.adaptador.web;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.estoque.aplicacao.RegistrarReposicao;
import br.ucsal.clinica.estoque.aplicacao.RegistrarRetiradaMedicamento;
import br.ucsal.clinica.estoque.dominio.MovimentacaoEstoque;
import br.ucsal.clinica.estoque.dominio.SaldoEstoque;
import br.ucsal.clinica.estoque.dominio.TipoMovimentacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public final class EstoqueDTO {

    private EstoqueDTO() {
    }

    /**
     * Secao 4.2.3: { "solicitacaoId": 42, "medicamentoId": 7, "quantidade": 2 }.
     * O responsavel pela entrega NAO e enviado: vem do usuario autenticado (RN018).
     */
    public record RetiradaRequest(
            @NotNull(message = "Informe a solicitação aprovada") Long solicitacaoId,
            Long medicamentoId,
            @Positive Integer quantidade) {

        public RegistrarRetiradaMedicamento.Comando paraComando() {
            return new RegistrarRetiradaMedicamento.Comando(solicitacaoId, medicamentoId, quantidade);
        }
    }

    /** Resposta da retirada no formato da secao 4.2.3. */
    public record RetiradaResponse(
            Long movimentacaoId,
            TipoMovimentacao tipo,
            String medicamento,
            int quantidade,
            String colaboradorSolicitante,
            String responsavelEntrega,
            OffsetDateTime dataHora,
            long saldoAtual,
            boolean reposicaoNecessaria) {

        public static RetiradaResponse de(RegistrarReposicao.Resultado r) {
            MovimentacaoEstoque m = r.movimentacao();
            return new RetiradaResponse(m.getId(), m.getTipo(), Referencia.nomeDe(m.getMedicamento()),
                    m.getQuantidade(), Referencia.nomeDe(m.getSolicitante()), Referencia.nomeDe(m.getResponsavelEntrega()),
                    comFuso(m.getDataHora()), r.saldoAtual(), r.reposicaoNecessaria());
        }
    }

    /** RF042: reposicao = entrada no estoque, com validade e data de aquisicao. */
    public record ReposicaoRequest(
            @NotNull(message = "Informe o medicamento") Long medicamentoId,
            @NotNull @Positive Integer quantidade,
            @Size(max = 40) String lote,
            @NotNull(message = "Informe a data de validade") LocalDate dataValidade,
            @NotNull(message = "Informe a data de aquisição") LocalDate dataAquisicao,
            String observacao) {

        public RegistrarReposicao.Comando paraComando() {
            return new RegistrarReposicao.Comando(medicamentoId, quantidade, lote, dataValidade, dataAquisicao,
                    observacao);
        }
    }

    public record ReposicaoResponse(MovimentacaoResponse movimentacao, long saldoAtual, boolean reposicaoNecessaria) {

        public static ReposicaoResponse de(RegistrarReposicao.Resultado r) {
            return new ReposicaoResponse(MovimentacaoResponse.de(r.movimentacao()), r.saldoAtual(),
                    r.reposicaoNecessaria());
        }
    }

    public record MovimentacaoResponse(
            Long id,
            Long medicamentoId,
            String medicamento,
            TipoMovimentacao tipo,
            int quantidade,
            OffsetDateTime dataHora,
            String lote,
            LocalDate dataValidade,
            LocalDate dataAquisicao,
            Long solicitacaoId,
            String colaboradorSolicitante,
            String responsavelEntrega,
            String observacao,
            String registradoPor) {

        public static MovimentacaoResponse de(MovimentacaoEstoque m) {
            return new MovimentacaoResponse(m.getId(), Referencia.idDe(m.getMedicamento()),
                    Referencia.nomeDe(m.getMedicamento()), m.getTipo(), m.getQuantidade(), comFuso(m.getDataHora()),
                    m.getLote(), m.getDataValidade(), m.getDataAquisicao(), m.getSolicitacaoId(),
                    Referencia.nomeDe(m.getSolicitante()), Referencia.nomeDe(m.getResponsavelEntrega()),
                    m.getObservacao(), m.getRegistradoPor());
        }
    }

    /** RF057 / RF058: saldo atual e indicacao de reposicao. */
    public record SaldoResponse(Long medicamentoId, String nome, String fabricante, int estoqueMinimo, long saldoAtual,
            boolean reposicaoNecessaria) {

        public static SaldoResponse de(SaldoEstoque s) {
            return new SaldoResponse(s.medicamentoId(), s.nome(), s.fabricante(), s.estoqueMinimo(), s.saldo(),
                    s.reposicaoNecessaria());
        }
    }

    static OffsetDateTime comFuso(LocalDateTime dataHora) {
        return dataHora == null ? null : dataHora.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }
}
