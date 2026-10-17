package br.ucsal.clinica.autenticacao.adaptador.persistencia;

import br.ucsal.clinica.auditoria.Rotulavel;
import br.ucsal.clinica.autenticacao.dominio.NomePerfil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Perfis de acesso (tb_perfil), usados pelo Spring Security (secao 6.1). */
@Getter
@Setter
@Entity
@Table(name = "tb_perfil")
public class PerfilEntity implements Rotulavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, unique = true)
    private NomePerfil nome;

    @Column(length = 200)
    private String descricao;

    @Override
    public String rotuloAuditoria() {
        return nome != null ? nome.name() : null;
    }
}
