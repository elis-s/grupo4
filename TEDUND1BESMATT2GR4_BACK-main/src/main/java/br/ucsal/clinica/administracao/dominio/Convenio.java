package br.ucsal.clinica.administracao.dominio;

import br.ucsal.clinica.compartilhado.dominio.EntidadeDominio;

/**
 * Convenio firmado com a clinica (RF007 a RF009).
 */
public class Convenio extends EntidadeDominio {

    private String razaoSocial;
    private String nomeFantasia;
    private String registroAns;
    private String cnpj;
    private String inscricaoMunicipal;
    private String codigoPrestador;
    private String email;
    private String telefone;

    /** RF008: edicao do convenio. */
    public void atualizarCom(Convenio dados) {
        this.razaoSocial = dados.getRazaoSocial();
        this.nomeFantasia = dados.getNomeFantasia();
        this.registroAns = dados.getRegistroAns();
        this.cnpj = dados.getCnpj();
        this.inscricaoMunicipal = dados.getInscricaoMunicipal();
        this.codigoPrestador = dados.getCodigoPrestador();
        this.email = dados.getEmail();
        this.telefone = dados.getTelefone();
    }

    // ---------------- getters e setters ----------------

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public String getRegistroAns() {
        return registroAns;
    }

    public void setRegistroAns(String registroAns) {
        this.registroAns = registroAns;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getInscricaoMunicipal() {
        return inscricaoMunicipal;
    }

    public void setInscricaoMunicipal(String inscricaoMunicipal) {
        this.inscricaoMunicipal = inscricaoMunicipal;
    }

    public String getCodigoPrestador() {
        return codigoPrestador;
    }

    public void setCodigoPrestador(String codigoPrestador) {
        this.codigoPrestador = codigoPrestador;
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
}
