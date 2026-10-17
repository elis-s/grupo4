package br.ucsal.clinica.administracao.dominio;

import br.ucsal.clinica.compartilhado.dominio.EntidadeDominio;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.compartilhado.dominio.Textos;

/**
 * Setor (espaco) da clinica (RF004 a RF006).
 */
public class Setor extends EntidadeDominio {

    private Referencia clinica;
    private String nome;
    private String email;
    private String telefone;
    private Referencia responsavel;

    /** RN004 e RN005: todo setor pertence a uma unica clinica e tem exatamente um responsavel. */
    public void validar() {
        if (clinica == null || clinica.id() == null) {
            throw new RegraNegocioException("Todo setor deve estar vinculado a uma clínica (RN004).");
        }
        if (responsavel == null || responsavel.id() == null) {
            throw new RegraNegocioException("Todo setor deve ter um responsável (RN005).");
        }
        nome = Textos.obrigatorio(nome, "Informe o nome do setor.");
    }

    /** RF005: edicao do setor. */
    public void atualizarCom(Setor dados) {
        this.clinica = dados.getClinica();
        this.nome = dados.getNome();
        this.email = dados.getEmail();
        this.telefone = dados.getTelefone();
        this.responsavel = dados.getResponsavel();
    }

    // ---------------- getters e setters ----------------

    public Referencia getClinica() {
        return clinica;
    }

    public void setClinica(Referencia clinica) {
        this.clinica = clinica;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Referencia getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(Referencia responsavel) {
        this.responsavel = responsavel;
    }
}
