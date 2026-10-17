package br.ucsal.clinica.autenticacao.dominio;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import br.ucsal.clinica.compartilhado.dominio.EntidadeDominio;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;

/**
 * Usuario de acesso ao sistema (RF013, RF044, RF063): sempre vinculado a um
 * colaborador (RN037), com um ou mais perfis (RN001). A senha so existe como hash
 * BCrypt (RNF013) - quem gera o hash e a porta CodificadorSenhaPort.
 */
public class Usuario extends EntidadeDominio {

    private String login;
    private String senhaHash;
    private Referencia colaborador;
    private String funcaoColaborador;
    private boolean colaboradorAtivo;
    private Set<NomePerfil> perfis = new LinkedHashSet<>();
    private LocalDateTime ultimoLogin;

    /** RN001 / RF013: define os perfis do usuario (pode acumular mais de um). */
    public void definirPerfis(Collection<NomePerfil> novos) {
        if (novos == null || novos.isEmpty()) {
            throw new RegraNegocioException("Informe ao menos um perfil de acesso.");
        }
        this.perfis = new LinkedHashSet<>(novos);
    }

    public boolean temPerfil(NomePerfil perfil) {
        return perfis.contains(perfil);
    }

    /** RN037: usuario inativo, ou vinculado a colaborador inativo, nao acessa o sistema. */
    public boolean podeAcessar() {
        return isAtivo() && colaborador != null && colaboradorAtivo;
    }

    /** RN037: todo usuario precisa estar vinculado a um colaborador cadastrado. */
    public void vincularColaborador(Referencia colaborador, String funcao, boolean ativo) {
        if (colaborador == null || colaborador.id() == null) {
            throw new RegraNegocioException("Todo usuário deve estar vinculado a um colaborador (RN037).");
        }
        if (!ativo) {
            throw new RegraNegocioException("O colaborador informado está inativo (RN037).");
        }
        this.colaborador = colaborador;
        this.funcaoColaborador = funcao;
        this.colaboradorAtivo = true;
    }

    // ---------------- getters e setters ----------------

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public Referencia getColaborador() {
        return colaborador;
    }

    public void setColaborador(Referencia colaborador) {
        this.colaborador = colaborador;
    }

    public String getFuncaoColaborador() {
        return funcaoColaborador;
    }

    public void setFuncaoColaborador(String funcaoColaborador) {
        this.funcaoColaborador = funcaoColaborador;
    }

    public boolean isColaboradorAtivo() {
        return colaboradorAtivo;
    }

    public void setColaboradorAtivo(boolean colaboradorAtivo) {
        this.colaboradorAtivo = colaboradorAtivo;
    }

    public Set<NomePerfil> getPerfis() {
        return perfis;
    }

    public void setPerfis(Set<NomePerfil> perfis) {
        this.perfis = perfis;
    }

    public LocalDateTime getUltimoLogin() {
        return ultimoLogin;
    }

    public void setUltimoLogin(LocalDateTime ultimoLogin) {
        this.ultimoLogin = ultimoLogin;
    }
}
