package br.ucsal.clinica.administracao.adaptador.persistencia;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Responsaveis (proprietarios) da clinica (tb_clinica_proprietario). */
@Getter
@Setter
@Entity
@Table(name = "tb_clinica_proprietario")
public class ClinicaProprietarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinica_id", nullable = false)
    private ClinicaEntity clinica;

    @Column(nullable = false, length = 150)
    private String nome;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 11)
    private String cpf;

    @Override
    public String toString() {
        return nome;
    }
}
