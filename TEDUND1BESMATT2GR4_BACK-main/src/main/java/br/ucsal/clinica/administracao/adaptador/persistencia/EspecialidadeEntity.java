package br.ucsal.clinica.administracao.adaptador.persistencia;

import br.ucsal.clinica.auditoria.AuditoriaEntityListener;
import br.ucsal.clinica.auditoria.Rotulavel;
import br.ucsal.clinica.compartilhado.dominio.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Especialidades medicas (tb_especialidade). */
@Getter
@Setter
@Entity
@Table(name = "tb_especialidade")
@EntityListeners(AuditoriaEntityListener.class)
public class EspecialidadeEntity implements Rotulavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status = Status.ATIVO;

    @Override
    public String rotuloAuditoria() {
        return nome;
    }
}
