package br.ucsal.clinica.pacientes.adaptador.persistencia;

import java.time.LocalDate;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import br.ucsal.clinica.administracao.adaptador.persistencia.ColaboradorEntity;
import br.ucsal.clinica.administracao.adaptador.persistencia.ConvenioEntity;
import br.ucsal.clinica.administracao.adaptador.persistencia.EspecialidadeEntity;
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

/** Mapeamento JPA do paciente (tb_paciente). */
@Getter
@Setter
@Entity
@Table(name = "tb_paciente")
public class PacienteEntity extends EntidadeJpa implements Rotulavel {

    @Column(name = "nome_completo", nullable = false, length = 150)
    private String nomeCompleto;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 11, unique = true)
    private String cpf;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 1)
    private String sexo;

    @Column(length = 20)
    private String telefone;

    @Column(length = 150)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id")
    private ColaboradorEntity medico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidade_id")
    private EspecialidadeEntity especialidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "convenio_id")
    private ConvenioEntity convenio;

    @Column(name = "numero_carteirinha", length = 40)
    private String numeroCarteirinha;

    @Override
    public String rotuloAuditoria() {
        return nomeCompleto;
    }
}
