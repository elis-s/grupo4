package br.ucsal.clinica.atendimento.adaptador.persistencia;

import java.time.LocalDateTime;

import org.hibernate.annotations.Immutable;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import br.ucsal.clinica.administracao.adaptador.persistencia.ColaboradorEntity;
import br.ucsal.clinica.auditoria.AuditoriaEntityListener;
import br.ucsal.clinica.compartilhado.dominio.Status;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Orientacoes de enfermagem (tb_orientacao_enfermagem - RF022): somente inclusao. */
@Getter
@Setter
@Immutable
@Entity
@Table(name = "tb_orientacao_enfermagem")
@EntityListeners({ AuditingEntityListener.class, AuditoriaEntityListener.class })
public class OrientacaoEnfermagemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atendimento_id", nullable = false)
    private AtendimentoEntity atendimento;

    /** Colaborador que registrou a orientacao. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medico_id", nullable = false)
    private ColaboradorEntity colaborador;

    @Column(nullable = false, columnDefinition = "text")
    private String orientacao;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @CreatedDate
    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status = Status.ATIVO;

    @CreatedBy
    @Column(name = "criado_por", length = 60, updatable = false)
    private String criadoPor;

    @PrePersist
    protected void garantirDatas() {
        LocalDateTime agora = LocalDateTime.now();
        if (dataCadastro == null) {
            dataCadastro = agora;
        }
        if (dataHora == null) {
            dataHora = agora;
        }
    }
}
