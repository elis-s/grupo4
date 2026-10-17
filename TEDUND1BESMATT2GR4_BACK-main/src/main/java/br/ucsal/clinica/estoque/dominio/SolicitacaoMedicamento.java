package br.ucsal.clinica.estoque.dominio;

import java.time.LocalDateTime;

import br.ucsal.clinica.compartilhado.dominio.EntidadeDominio;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.compartilhado.dominio.Textos;

/**
 * Solicitacao de medicamento feita pelo colaborador (RF023, RF038) e aprovada ou
 * reprovada pelo usuario/atendente antes da retirada (RF059, RN033).
 */
public class SolicitacaoMedicamento extends EntidadeDominio {

    private Referencia medicamento;
    private Referencia solicitante;
    private Long atendimentoId;
    private int quantidade;
    private String justificativa;
    private SituacaoSolicitacao situacao = SituacaoSolicitacao.PENDENTE;
    private Referencia analisadoPor;
    private LocalDateTime dataAnalise;
    private String motivoReprovacao;

    /** RF023 / RN019: medicamento, quantidade, justificativa, data de cadastro e status (PENDENTE). */
    public static SolicitacaoMedicamento nova(Referencia medicamento, Referencia solicitante, int quantidade,
            String justificativa, Long atendimentoId) {
        if (medicamento == null || medicamento.id() == null) {
            throw new RegraNegocioException("Informe o medicamento solicitado.");
        }
        if (solicitante == null || solicitante.id() == null) {
            throw new RegraNegocioException("A solicitação precisa identificar o colaborador solicitante (RF038).");
        }
        if (quantidade <= 0) {
            throw new RegraNegocioException("A quantidade solicitada deve ser maior que zero.");
        }
        SolicitacaoMedicamento s = new SolicitacaoMedicamento();
        s.medicamento = medicamento;
        s.solicitante = solicitante;
        s.quantidade = quantidade;
        s.justificativa = Textos.obrigatorio(justificativa, "Informe a justificativa da solicitação.");
        s.atendimentoId = atendimentoId;
        s.situacao = SituacaoSolicitacao.PENDENTE;
        return s;
    }

    /** RF059 / RN033: o usuario (atendente) aprova a solicitacao. */
    public void aprovar(Referencia usuario) {
        garantirPendente();
        this.situacao = SituacaoSolicitacao.APROVADA;
        this.analisadoPor = usuario;
        this.dataAnalise = LocalDateTime.now();
    }

    /** RF059 / RN033: o usuario (atendente) reprova a solicitacao, com o motivo. */
    public void reprovar(Referencia usuario, String motivo) {
        garantirPendente();
        this.motivoReprovacao = Textos.obrigatorio(motivo, "Informe o motivo da reprovação.");
        this.situacao = SituacaoSolicitacao.REPROVADA;
        this.analisadoPor = usuario;
        this.dataAnalise = LocalDateTime.now();
    }

    /** A retirada so acontece para solicitacao aprovada; depois dela a solicitacao fica ENTREGUE. */
    public void registrarEntrega() {
        if (!isAtivo()) {
            throw new RegraNegocioException("Esta solicitação está inativa.");
        }
        if (situacao != SituacaoSolicitacao.APROVADA) {
            throw new RegraNegocioException("Só é possível retirar medicamento de uma solicitação APROVADA (situação atual: "
                    + situacao + ").");
        }
        this.situacao = SituacaoSolicitacao.ENTREGUE;
    }

    private void garantirPendente() {
        if (!isAtivo()) {
            throw new RegraNegocioException("Esta solicitação está inativa.");
        }
        if (situacao != SituacaoSolicitacao.PENDENTE) {
            throw new RegraNegocioException("Esta solicitação já foi analisada (situação: " + situacao + ").");
        }
    }

    // ---------------- getters e setters ----------------

    public Referencia getMedicamento() {
        return medicamento;
    }

    public void setMedicamento(Referencia medicamento) {
        this.medicamento = medicamento;
    }

    public Referencia getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Referencia solicitante) {
        this.solicitante = solicitante;
    }

    public Long getAtendimentoId() {
        return atendimentoId;
    }

    public void setAtendimentoId(Long atendimentoId) {
        this.atendimentoId = atendimentoId;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public SituacaoSolicitacao getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoSolicitacao situacao) {
        this.situacao = situacao;
    }

    public Referencia getAnalisadoPor() {
        return analisadoPor;
    }

    public void setAnalisadoPor(Referencia analisadoPor) {
        this.analisadoPor = analisadoPor;
    }

    public LocalDateTime getDataAnalise() {
        return dataAnalise;
    }

    public void setDataAnalise(LocalDateTime dataAnalise) {
        this.dataAnalise = dataAnalise;
    }

    public String getMotivoReprovacao() {
        return motivoReprovacao;
    }

    public void setMotivoReprovacao(String motivoReprovacao) {
        this.motivoReprovacao = motivoReprovacao;
    }
}
