package br.ucsal.clinica.administracao.adaptador.persistencia;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import br.ucsal.clinica.auditoria.Rotulavel;
import br.ucsal.clinica.compartilhado.adaptador.persistencia.EntidadeJpa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Mapeamento JPA do convenio (tb_convenio). */
@Getter
@Setter
@Entity
@Table(name = "tb_convenio")
public class ConvenioEntity extends EntidadeJpa implements Rotulavel {

    @Column(name = "razao_social", nullable = false, length = 200)
    private String razaoSocial;

    @Column(name = "nome_fantasia", nullable = false, length = 200)
    private String nomeFantasia;

    @Column(name = "registro_ans", nullable = false, length = 20, unique = true)
    private String registroAns;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 14, unique = true)
    private String cnpj;

    @Column(name = "inscricao_municipal", length = 30)
    private String inscricaoMunicipal;

    @Column(name = "codigo_prestador", nullable = false, length = 30)
    private String codigoPrestador;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Override
    public String rotuloAuditoria() {
        return nomeFantasia;
    }
}
