package br.ucsal.clinica.administracao.adaptador.persistencia;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import br.ucsal.clinica.administracao.dominio.Funcao;
import br.ucsal.clinica.auditoria.Rotulavel;
import br.ucsal.clinica.compartilhado.adaptador.persistencia.EntidadeJpa;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Mapeamento JPA do colaborador (tb_colaborador) - corpo clinico e demais profissionais. */
@Getter
@Setter
@Entity
@Table(name = "tb_colaborador")
public class ColaboradorEntity extends EntidadeJpa implements Rotulavel {

    @Column(name = "nome_completo", nullable = false, length = 150)
    private String nomeCompleto;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 11, unique = true)
    private String cpf;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Funcao funcao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "setor_id", nullable = false)
    private SetorEntity setor;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    @Column(name = "carga_horaria_semanal", nullable = false)
    private Short cargaHorariaSemanal;

    @Column(name = "conselho_sigla", length = 10)
    private String conselhoSigla;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "conselho_uf", length = 2)
    private String conselhoUf;

    @Column(name = "conselho_numero", length = 20)
    private String conselhoNumero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidade_id")
    private EspecialidadeEntity especialidade;

    @OneToMany(mappedBy = "colaborador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ColaboradorTurnoEntity> turnos = new ArrayList<>();

    @Override
    public String rotuloAuditoria() {
        return nomeCompleto;
    }
}
