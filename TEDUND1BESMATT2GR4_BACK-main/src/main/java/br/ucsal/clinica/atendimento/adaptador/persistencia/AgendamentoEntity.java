package br.ucsal.clinica.atendimento.adaptador.persistencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import br.ucsal.clinica.administracao.adaptador.persistencia.ColaboradorEntity;
import br.ucsal.clinica.atendimento.dominio.SituacaoAgendamento;
import br.ucsal.clinica.auditoria.Rotulavel;
import br.ucsal.clinica.compartilhado.adaptador.persistencia.EntidadeJpa;
import br.ucsal.clinica.pacientes.adaptador.persistencia.PacienteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Mapeamento JPA do agendamento (tb_agendamento). */
@Getter
@Setter
@Entity
@Table(name = "tb_agendamento")
public class AgendamentoEntity extends EntidadeJpa implements Rotulavel {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medico_id", nullable = false)
    private ColaboradorEntity medico;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private PacienteEntity paciente;

    @Column(name = "data_prevista", nullable = false)
    private LocalDate dataPrevista;

    @Column(name = "hora_prevista", nullable = false)
    private LocalTime horaPrevista;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private SituacaoAgendamento situacao = SituacaoAgendamento.AGENDADO;

    @Column(columnDefinition = "text")
    private String observacao;

    @Column(name = "data_recepcao")
    private LocalDateTime dataRecepcao;

    @Override
    public String rotuloAuditoria() {
        return dataPrevista + " " + horaPrevista;
    }
}
