package br.ucsal.clinica.estoque.adaptador.persistencia;

import java.time.LocalDateTime;

import br.ucsal.clinica.administracao.adaptador.persistencia.ColaboradorEntity;
import br.ucsal.clinica.auditoria.Rotulavel;
import br.ucsal.clinica.autenticacao.adaptador.persistencia.UsuarioEntity;
import br.ucsal.clinica.compartilhado.adaptador.persistencia.EntidadeJpa;
import br.ucsal.clinica.estoque.dominio.SituacaoSolicitacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Solicitacoes de medicamento (tb_solicitacao_medicamento). */
@Getter
@Setter
@Entity
@Table(name = "tb_solicitacao_medicamento")
public class SolicitacaoMedicamentoEntity extends EntidadeJpa implements Rotulavel {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private MedicamentoEntity medicamento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private ColaboradorEntity solicitante;

    /** Atendimento de origem (opcional). Guardado so pelo id para nao acoplar os modulos. */
    @Column(name = "atendimento_id")
    private Long atendimentoId;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false, columnDefinition = "text")
    private String justificativa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SituacaoSolicitacao situacao = SituacaoSolicitacao.PENDENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analisado_por_id")
    private UsuarioEntity analisadoPor;

    @Column(name = "data_analise")
    private LocalDateTime dataAnalise;

    @Column(name = "motivo_reprovacao", columnDefinition = "text")
    private String motivoReprovacao;

    @Override
    public String rotuloAuditoria() {
        return quantidade + " x " + (medicamento != null ? medicamento.getNome() : "");
    }
}
