package br.ucsal.clinica.administracao.adaptador.persistencia;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import br.ucsal.clinica.auditoria.Rotulavel;
import br.ucsal.clinica.compartilhado.adaptador.persistencia.EntidadeJpa;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Mapeamento JPA da clinica (tb_clinica). */
@Getter
@Setter
@Entity
@Table(name = "tb_clinica")
public class ClinicaEntity extends EntidadeJpa implements Rotulavel {

    @Column(name = "razao_social", nullable = false, length = 200)
    private String razaoSocial;

    @Column(name = "nome_fantasia", nullable = false, length = 200)
    private String nomeFantasia;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 14, unique = true)
    private String cnpj;

    @Column(nullable = false, length = 200)
    private String logradouro;

    @Column(length = 20)
    private String numero;

    @Column(length = 100)
    private String complemento;

    @Column(nullable = false, length = 100)
    private String bairro;

    @Column(nullable = false, length = 100)
    private String cidade;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 2)
    private String estado;

    @Column(nullable = false, length = 60)
    private String pais = "Brasil";

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 8)
    private String cep;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "atividade_principal", nullable = false, length = 200)
    private String atividadePrincipal;

    @Column(columnDefinition = "text")
    private String missao;

    @Column(name = "alvara_numero", nullable = false, length = 50)
    private String alvaraNumero;

    @Column(name = "alvara_data_emissao")
    private LocalDate alvaraDataEmissao;

    @Column(name = "alvara_data_validade")
    private LocalDate alvaraDataValidade;

    @Column(name = "licenca_sanitaria_numero", nullable = false, length = 50)
    private String licencaSanitariaNumero;

    @Column(name = "licenca_sanitaria_emissao")
    private LocalDate licencaSanitariaEmissao;

    @Column(name = "licenca_sanitaria_validade")
    private LocalDate licencaSanitariaValidade;

    @Column(name = "registro_crm", nullable = false, length = 30)
    private String registroCrm;

    @Column(name = "diretor_tecnico_nome", nullable = false, length = 150)
    private String diretorTecnicoNome;

    @Column(name = "diretor_tecnico_crm", length = 30)
    private String diretorTecnicoCrm;

    @OneToMany(mappedBy = "clinica", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ClinicaTelefoneEntity> telefones = new ArrayList<>();

    @OneToMany(mappedBy = "clinica", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ClinicaProprietarioEntity> proprietarios = new ArrayList<>();

    @Override
    public String rotuloAuditoria() {
        return nomeFantasia;
    }
}
