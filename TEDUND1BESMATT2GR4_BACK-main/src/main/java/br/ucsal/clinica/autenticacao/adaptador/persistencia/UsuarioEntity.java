package br.ucsal.clinica.autenticacao.adaptador.persistencia;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import br.ucsal.clinica.administracao.adaptador.persistencia.ColaboradorEntity;
import br.ucsal.clinica.auditoria.Rotulavel;
import br.ucsal.clinica.compartilhado.adaptador.persistencia.EntidadeJpa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Usuarios (tb_usuario) e o vinculo usuario-perfil (tb_usuario_perfil - muitos-para-muitos,
 * um usuario pode acumular perfis). Lidos pelo UserDetailsService do Spring Security.
 */
@Getter
@Setter
@Entity
@Table(name = "tb_usuario")
public class UsuarioEntity extends EntidadeJpa implements Rotulavel {

    @Column(nullable = false, length = 60, unique = true)
    private String login;

    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "colaborador_id", nullable = false, unique = true)
    private ColaboradorEntity colaborador;

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tb_usuario_perfil",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "perfil_id"))
    private Set<PerfilEntity> perfis = new HashSet<>();

    @Override
    public String rotuloAuditoria() {
        return login;
    }
}
