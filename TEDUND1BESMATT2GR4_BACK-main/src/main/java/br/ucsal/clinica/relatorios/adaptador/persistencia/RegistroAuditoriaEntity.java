package br.ucsal.clinica.relatorios.adaptador.persistencia;

import java.time.LocalDateTime;

import org.hibernate.annotations.Immutable;

import br.ucsal.clinica.relatorios.dominio.AcaoAuditoria;
import br.ucsal.clinica.relatorios.dominio.ResultadoAuditoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** tb_auditoria: append-only (RN036) - @Immutable e sem permissao de UPDATE/DELETE no banco. */
@Getter
@Setter
@Immutable
@Entity
@Table(name = "tb_auditoria")
public class RegistroAuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(length = 60)
    private String login;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AcaoAuditoria acao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ResultadoAuditoria resultado;

    @Column(length = 60)
    private String entidade;

    @Column(name = "entidade_id")
    private Long entidadeId;

    @Column(columnDefinition = "text")
    private String descricao;

    @Column(name = "ip_origem", length = 45)
    private String ipOrigem;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;
}
