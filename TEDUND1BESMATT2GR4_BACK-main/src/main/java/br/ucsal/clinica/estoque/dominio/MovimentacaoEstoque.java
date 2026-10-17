package br.ucsal.clinica.estoque.dominio;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.compartilhado.dominio.Textos;

/**
 * Movimentacao de estoque (RF036, RF039): historico imutavel - depois de gravada
 * nao e alterada nem excluida. O saldo do medicamento so muda por aqui.
 */
public class MovimentacaoEstoque {

    private Long id;
    private Referencia medicamento;
    private TipoMovimentacao tipo;
    private int quantidade;
    private LocalDateTime dataHora;
    private String lote;
    private LocalDate dataValidade;
    private LocalDate dataAquisicao;
    private Long solicitacaoId;
    private Referencia solicitante;
    private Referencia responsavelEntrega;
    private String observacao;
    private String registradoPor;

    /**
     * RF042 / RN017 / RN031: reposicao = movimentacao de ENTRADA, a qualquer tempo,
     * com validade e data de aquisicao.
     */
    public static MovimentacaoEstoque entrada(Referencia medicamento, int quantidade, String lote,
            LocalDate dataValidade, LocalDate dataAquisicao, String observacao) {
        if (quantidade <= 0) {
            throw new RegraNegocioException("A quantidade da entrada deve ser maior que zero.");
        }
        if (dataValidade == null || dataAquisicao == null) {
            throw new RegraNegocioException("Informe a data de validade e a data de aquisição.");
        }
        if (dataValidade.isBefore(dataAquisicao)) {
            throw new RegraNegocioException("A data de validade não pode ser anterior à data de aquisição.");
        }
        MovimentacaoEstoque m = new MovimentacaoEstoque();
        m.medicamento = medicamento;
        m.tipo = TipoMovimentacao.ENTRADA;
        m.quantidade = quantidade;
        m.dataHora = LocalDateTime.now(); // RN014
        m.lote = Textos.vazioParaNulo(lote);
        m.dataValidade = dataValidade;
        m.dataAquisicao = dataAquisicao;
        m.observacao = Textos.vazioParaNulo(observacao);
        return m;
    }

    /**
     * RF037 a RF040 / RN013 / RN014 / RN018: retirada = movimentacao de SAIDA, sempre com o
     * colaborador que solicitou, o responsavel pela entrega (usuario autenticado) e a data/hora.
     */
    public static MovimentacaoEstoque saida(Referencia medicamento, int quantidade, Long solicitacaoId,
            Referencia solicitante, Referencia responsavelEntrega, String observacao) {
        if (quantidade <= 0) {
            throw new RegraNegocioException("A quantidade retirada deve ser maior que zero.");
        }
        if (solicitante == null || solicitante.id() == null) {
            throw new RegraNegocioException("Toda retirada deve registrar o colaborador que solicitou (RN013).");
        }
        if (responsavelEntrega == null || responsavelEntrega.id() == null) {
            throw new RegraNegocioException("Toda retirada deve registrar o responsável pela entrega (RN018).");
        }
        MovimentacaoEstoque m = new MovimentacaoEstoque();
        m.medicamento = medicamento;
        m.tipo = TipoMovimentacao.SAIDA;
        m.quantidade = quantidade;
        m.dataHora = LocalDateTime.now(); // RN014
        m.solicitacaoId = solicitacaoId;
        m.solicitante = solicitante;
        m.responsavelEntrega = responsavelEntrega;
        m.observacao = Textos.vazioParaNulo(observacao);
        return m;
    }

    // ---------------- getters e setters ----------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Referencia getMedicamento() {
        return medicamento;
    }

    public void setMedicamento(Referencia medicamento) {
        this.medicamento = medicamento;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimentacao tipo) {
        this.tipo = tipo;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(LocalDate dataValidade) {
        this.dataValidade = dataValidade;
    }

    public LocalDate getDataAquisicao() {
        return dataAquisicao;
    }

    public void setDataAquisicao(LocalDate dataAquisicao) {
        this.dataAquisicao = dataAquisicao;
    }

    public Long getSolicitacaoId() {
        return solicitacaoId;
    }

    public void setSolicitacaoId(Long solicitacaoId) {
        this.solicitacaoId = solicitacaoId;
    }

    public Referencia getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Referencia solicitante) {
        this.solicitante = solicitante;
    }

    public Referencia getResponsavelEntrega() {
        return responsavelEntrega;
    }

    public void setResponsavelEntrega(Referencia responsavelEntrega) {
        this.responsavelEntrega = responsavelEntrega;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(String registradoPor) {
        this.registradoPor = registradoPor;
    }
}
