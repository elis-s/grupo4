package br.ucsal.clinica.atendimento.adaptador.persistencia;

import br.ucsal.clinica.auditoria.AuditoriaEntityListener;
import br.ucsal.clinica.auditoria.Rotulavel;
import br.ucsal.clinica.estoque.adaptador.persistencia.MedicamentoEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Medicamentos prescritos no atendimento, com dosagem (tb_medicacao_atendimento - RF020). */
@Getter
@Setter
@Entity
@Table(name = "tb_medicacao_atendimento")
@EntityListeners(AuditoriaEntityListener.class)
public class PrescricaoMedicaEntity implements Rotulavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atendimento_id", nullable = false)
    private AtendimentoEntity atendimento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private MedicamentoEntity medicamento;

    @Column(nullable = false, length = 100)
    private String dosagem;

    @Column(length = 200)
    private String posologia;

    @Override
    public String rotuloAuditoria() {
        return (medicamento != null ? medicamento.getNome() : "") + " - " + dosagem;
    }
}
