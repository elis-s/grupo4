package br.ucsal.clinica.autenticacao.adaptador.persistencia;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Tokens revogados no logout (tb_token_revogado - secao 6.1). */
@Getter
@Setter
@Entity
@Table(name = "tb_token_revogado")
public class TokenRevogadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64, unique = true)
    private String jti;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    @Column(name = "data_revogacao", nullable = false)
    private LocalDateTime dataRevogacao;
}
