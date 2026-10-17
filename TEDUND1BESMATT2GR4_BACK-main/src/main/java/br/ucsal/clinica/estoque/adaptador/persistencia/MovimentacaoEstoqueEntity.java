package br.ucsal.clinica.estoque.adaptador.persistencia;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.Immutable;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import br.ucsal.clinica.administracao.adaptador.persistencia.ColaboradorEntity;
import br.ucsal.clinica.auditoria.AuditoriaEntityListener;
import br.ucsal.clinica.auditoria.Rotulavel;
import br.ucsal.clinica.autenticacao.adaptador.persistencia.UsuarioEntity;
import br.ucsal.clinica.estoque.dominio.TipoMovimentacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Movimentacoes de estoque (tb_movimentacao_estoque): somente inclusao (@Immutable + permissao no banco). */
@Getter
@Setter
@Immutable
@Entity
@Table(name = "tb_movimentacao_estoque")
@EntityListeners({ AuditingEntityListener.class, AuditoriaEntityListener.class })
public class MovimentacaoEstoqueEntity implements Rotulavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private MedicamentoEntity medicamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 7)
    private TipoMovimentacao tipo;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(length = 40)
    private String lote;

    @Column(name = "data_validade")
    private LocalDate dataValidade;

    @Column(name = "data_aquisicao")
    private LocalDate dataAquisicao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitacao_id")
    private SolicitacaoMedicamentoEntity solicitacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id")
    private ColaboradorEntity solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entregue_por_id")
    private UsuarioEntity entreguePor;

    @Column(columnDefinition = "text")
    private String observacao;

    @CreatedBy
    @Column(name = "criado_por", length = 60, updatable = false)
    private String criadoPor;

    @Override
    public String rotuloAuditoria() {
        return tipo + " de " + quantidade + " - " + (medicamento != null ? medicamento.getNome() : "");
    }
}
