package br.ucsal.clinica.pacientes.adaptador.persistencia;

import br.ucsal.clinica.auditoria.Rotulavel;
import br.ucsal.clinica.compartilhado.adaptador.persistencia.EntidadeJpa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Secao 3.4.2: ProntuarioEntity, da camada de infraestrutura, convertida pelo adaptador
 * para a entidade de dominio ProntuarioEletronico. Relacao 1:1 estrita com o paciente:
 * paciente_id e chave estrangeira NOT NULL e UNIQUE.
 */
@Getter
@Setter
@Entity
@Table(name = "tb_prontuario")
public class ProntuarioEntity extends EntidadeJpa implements Rotulavel {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", unique = true, nullable = false)
    private PacienteEntity paciente;

    @Column(nullable = false, length = 20, unique = true)
    private String numero;

    @Override
    public String rotuloAuditoria() {
        return numero;
    }
}
