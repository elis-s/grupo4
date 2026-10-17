package br.ucsal.clinica.administracao.adaptador.persistencia;

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

/** Telefones de contato da clinica (tb_clinica_telefone). */
@Getter
@Setter
@Entity
@Table(name = "tb_clinica_telefone")
public class ClinicaTelefoneEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinica_id", nullable = false)
    private ClinicaEntity clinica;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(length = 50)
    private String descricao;

    @Override
    public String toString() {
        return telefone + (descricao != null ? " (" + descricao + ")" : "");
    }
}
