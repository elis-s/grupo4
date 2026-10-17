package br.ucsal.clinica.atendimento.adaptador.persistencia;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.ucsal.clinica.administracao.adaptador.persistencia.ColaboradorEntity;
import br.ucsal.clinica.administracao.adaptador.persistencia.ConvenioEntity;
import br.ucsal.clinica.atendimento.dominio.EtapaAtendimento;
import br.ucsal.clinica.atendimento.dominio.TipoAtendimento;
import br.ucsal.clinica.auditoria.Rotulavel;
import br.ucsal.clinica.compartilhado.adaptador.persistencia.EntidadeJpa;
import br.ucsal.clinica.pacientes.adaptador.persistencia.ProntuarioEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Mapeamento JPA do atendimento clinico (tb_atendimento). */
@Getter
@Setter
@Entity
@Table(name = "tb_atendimento")
public class AtendimentoEntity extends EntidadeJpa implements Rotulavel {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prontuario_id", nullable = false)
    private ProntuarioEntity prontuario;

    /** Colaborador responsavel, capturado do usuario autenticado (RN028). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medico_id", nullable = false)
    private ColaboradorEntity medico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamento_id", unique = true)
    private AgendamentoEntity agendamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "convenio_id")
    private ConvenioEntity convenio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private TipoAtendimento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EtapaAtendimento etapa = EtapaAtendimento.EM_TRIAGEM;

    @Column(name = "data_hora_inicio", nullable = false)
    private LocalDateTime dataHoraInicio;

    @Column(nullable = false, columnDefinition = "text")
    private String sintomas;

    @Column(columnDefinition = "text")
    private String diagnostico;

    @Column(columnDefinition = "text")
    private String tratamento;

    @Column(name = "data_hora_encerramento")
    private LocalDateTime dataHoraEncerramento;

    @Column(nullable = false)
    private boolean internado;

    @OneToMany(mappedBy = "atendimento", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @OrderBy("id")
    private List<PrescricaoMedicaEntity> prescricoes = new ArrayList<>();

    @Override
    public String rotuloAuditoria() {
        return tipo + " - " + (prontuario != null ? prontuario.getNumero() : "");
    }
}
