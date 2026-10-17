package br.ucsal.clinica.administracao.adaptador.persistencia;

import br.ucsal.clinica.auditoria.Rotulavel;
import br.ucsal.clinica.compartilhado.adaptador.persistencia.EntidadeJpa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Mapeamento JPA do setor (tb_setor): uma clinica e um responsavel (RN004, RN005). */
@Getter
@Setter
@Entity
@Table(name = "tb_setor")
public class SetorEntity extends EntidadeJpa implements Rotulavel {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinica_id", nullable = false)
    private ClinicaEntity clinica;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String telefone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private ColaboradorEntity responsavel;

    @Override
    public String rotuloAuditoria() {
        return nome;
    }
}
