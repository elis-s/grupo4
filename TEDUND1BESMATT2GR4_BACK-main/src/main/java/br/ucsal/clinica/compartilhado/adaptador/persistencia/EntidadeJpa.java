package br.ucsal.clinica.compartilhado.adaptador.persistencia;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import br.ucsal.clinica.auditoria.AuditoriaEntityListener;
import br.ucsal.clinica.auditoria.EstadoAuditavel;
import br.ucsal.clinica.compartilhado.dominio.ConflitoVersaoException;
import br.ucsal.clinica.compartilhado.dominio.EntidadeDominio;
import br.ucsal.clinica.compartilhado.dominio.Status;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

/**
 * Base das entidades JPA (adaptador de persistencia - secao 5.2). Fica na
 * infraestrutura: as anotacoes do ORM nao entram no nucleo de dominio (secao 3.4.2).
 *
 * Colunas comuns das tabelas de dominio (secao 5.3): status, data de cadastro,
 * data e motivo da inativacao, versao (@Version - bloqueio otimista, RNF021) e quem
 * criou/alterou, preenchidos pelo AuditingEntityListener do Spring Data (secao 3.4.3).
 * O AuditoriaEntityListener grava o rastro em tb_auditoria (RF046).
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners({ AuditingEntityListener.class, AuditoriaEntityListener.class })
public abstract class EntidadeJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private Long versao;

    @CreatedDate
    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status = Status.ATIVO;

    @Column(name = "motivo_inativacao", columnDefinition = "text")
    private String motivoInativacao;

    @Column(name = "data_inativacao")
    private LocalDateTime dataInativacao;

    @CreatedBy
    @Column(name = "criado_por", length = 60, updatable = false)
    private String criadoPor;

    @LastModifiedBy
    @Column(name = "atualizado_por", length = 60)
    private String atualizadoPor;

    @LastModifiedDate
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    /** Estado antes da alteracao, para a auditoria registrar o que mudou (delta - secao 3.4.3). */
    @Transient
    private transient Map<String, String> estadoAnterior;

    @PrePersist
    protected void garantirPadroes() {
        if (dataCadastro == null) {
            dataCadastro = LocalDateTime.now();
        }
        if (status == null) {
            status = Status.ATIVO;
        }
    }

    /** Fotografa o estado atual antes de aplicar as alteracoes vindas do dominio. */
    public void capturarEstadoAnterior() {
        this.estadoAnterior = EstadoAuditavel.de(this);
    }

    /**
     * Prepara a entidade gerenciada para receber as alteracoes do dominio:
     * confere a versao (bloqueio otimista), guarda o estado anterior para a
     * auditoria e marca a data de atualizacao.
     */
    public void prepararAlteracao(EntidadeDominio dominio) {
        if (dominio.getVersao() != null && versao != null && !dominio.getVersao().equals(versao)) {
            throw new ConflitoVersaoException();
        }
        capturarEstadoAnterior();
        this.dataAtualizacao = LocalDateTime.now();
        receberStatusDe(dominio);
    }

    /** Copia os campos de controle (id, versao, status...) para a entidade de dominio. */
    public void copiarControlePara(EntidadeDominio dominio) {
        dominio.restaurarControle(id, versao, dataCadastro, status, motivoInativacao, dataInativacao);
    }

    /** Recebe do dominio o status e os dados da inativacao. */
    public void receberStatusDe(EntidadeDominio dominio) {
        this.status = dominio.getStatus();
        this.motivoInativacao = dominio.getMotivoInativacao();
        this.dataInativacao = dominio.getDataInativacao();
    }
}
