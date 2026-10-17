package br.ucsal.clinica.administracao.dominio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import br.ucsal.clinica.compartilhado.dominio.EntidadeDominio;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.compartilhado.dominio.Textos;

/**
 * Clinica (RF001 a RF003): dados cadastrais, endereco, contatos, responsaveis,
 * atividade principal, missao, licencas, registro no CRM e diretor tecnico.
 * Uma clinica com historico nao e excluida, apenas inativada com motivo (RN021, RN023).
 */
public class Clinica extends EntidadeDominio {

    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String pais;
    private String cep;
    private String email;
    private String atividadePrincipal;
    private String missao;
    private String alvaraNumero;
    private LocalDate alvaraDataEmissao;
    private LocalDate alvaraDataValidade;
    private String licencaSanitariaNumero;
    private LocalDate licencaSanitariaEmissao;
    private LocalDate licencaSanitariaValidade;
    private String registroCrm;
    private String diretorTecnicoNome;
    private String diretorTecnicoCrm;
    private List<TelefoneClinica> telefones = new ArrayList<>();
    private List<ResponsavelClinica> responsaveis = new ArrayList<>();

    /** RF001: valida os dados obrigatorios da clinica. */
    public void validar() {
        if (Textos.vazio(pais)) {
            pais = "Brasil";
        }
        if (telefones == null || telefones.isEmpty()) {
            throw new RegraNegocioException("Informe ao menos um telefone de contato da clínica.");
        }
        if (responsaveis == null || responsaveis.isEmpty()) {
            throw new RegraNegocioException("Informe ao menos um responsável (proprietário) da clínica.");
        }
        validarPeriodo(alvaraDataEmissao, alvaraDataValidade, "do alvará");
        validarPeriodo(licencaSanitariaEmissao, licencaSanitariaValidade, "da licença sanitária");
    }

    /** RF002: edicao dos dados cadastrais. */
    public void atualizarCom(Clinica dados) {
        this.razaoSocial = dados.getRazaoSocial();
        this.nomeFantasia = dados.getNomeFantasia();
        this.cnpj = dados.getCnpj();
        this.logradouro = dados.getLogradouro();
        this.numero = dados.getNumero();
        this.complemento = dados.getComplemento();
        this.bairro = dados.getBairro();
        this.cidade = dados.getCidade();
        this.estado = dados.getEstado();
        this.pais = dados.getPais();
        this.cep = dados.getCep();
        this.email = dados.getEmail();
        this.atividadePrincipal = dados.getAtividadePrincipal();
        this.missao = dados.getMissao();
        this.alvaraNumero = dados.getAlvaraNumero();
        this.alvaraDataEmissao = dados.getAlvaraDataEmissao();
        this.alvaraDataValidade = dados.getAlvaraDataValidade();
        this.licencaSanitariaNumero = dados.getLicencaSanitariaNumero();
        this.licencaSanitariaEmissao = dados.getLicencaSanitariaEmissao();
        this.licencaSanitariaValidade = dados.getLicencaSanitariaValidade();
        this.registroCrm = dados.getRegistroCrm();
        this.diretorTecnicoNome = dados.getDiretorTecnicoNome();
        this.diretorTecnicoCrm = dados.getDiretorTecnicoCrm();
        this.telefones = dados.getTelefones();
        this.responsaveis = dados.getResponsaveis();
    }

    private static void validarPeriodo(LocalDate emissao, LocalDate validade, String documento) {
        if (emissao != null && validade != null && validade.isBefore(emissao)) {
            throw new RegraNegocioException("A validade " + documento + " não pode ser anterior à emissão.");
        }
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

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAtividadePrincipal() {
        return atividadePrincipal;
    }

    public void setAtividadePrincipal(String atividadePrincipal) {
        this.atividadePrincipal = atividadePrincipal;
    }

    public String getMissao() {
        return missao;
    }

    public void setMissao(String missao) {
        this.missao = missao;
    }

    public String getAlvaraNumero() {
        return alvaraNumero;
    }

    public void setAlvaraNumero(String alvaraNumero) {
        this.alvaraNumero = alvaraNumero;
    }

    public LocalDate getAlvaraDataEmissao() {
        return alvaraDataEmissao;
    }

    public void setAlvaraDataEmissao(LocalDate alvaraDataEmissao) {
        this.alvaraDataEmissao = alvaraDataEmissao;
    }

    public LocalDate getAlvaraDataValidade() {
        return alvaraDataValidade;
    }

    public void setAlvaraDataValidade(LocalDate alvaraDataValidade) {
        this.alvaraDataValidade = alvaraDataValidade;
    }

    public String getLicencaSanitariaNumero() {
        return licencaSanitariaNumero;
    }

    public void setLicencaSanitariaNumero(String licencaSanitariaNumero) {
        this.licencaSanitariaNumero = licencaSanitariaNumero;
    }

    public LocalDate getLicencaSanitariaEmissao() {
        return licencaSanitariaEmissao;
    }

    public void setLicencaSanitariaEmissao(LocalDate licencaSanitariaEmissao) {
        this.licencaSanitariaEmissao = licencaSanitariaEmissao;
    }

    public LocalDate getLicencaSanitariaValidade() {
        return licencaSanitariaValidade;
    }

    public void setLicencaSanitariaValidade(LocalDate licencaSanitariaValidade) {
        this.licencaSanitariaValidade = licencaSanitariaValidade;
    }

    public String getRegistroCrm() {
        return registroCrm;
    }

    public void setRegistroCrm(String registroCrm) {
        this.registroCrm = registroCrm;
    }

    public String getDiretorTecnicoNome() {
        return diretorTecnicoNome;
    }

    public void setDiretorTecnicoNome(String diretorTecnicoNome) {
        this.diretorTecnicoNome = diretorTecnicoNome;
    }

    public String getDiretorTecnicoCrm() {
        return diretorTecnicoCrm;
    }

    public void setDiretorTecnicoCrm(String diretorTecnicoCrm) {
        this.diretorTecnicoCrm = diretorTecnicoCrm;
    }

    public List<TelefoneClinica> getTelefones() {
        return telefones;
    }

    public void setTelefones(List<TelefoneClinica> telefones) {
        this.telefones = telefones;
    }

    public List<ResponsavelClinica> getResponsaveis() {
        return responsaveis;
    }

    public void setResponsaveis(List<ResponsavelClinica> responsaveis) {
        this.responsaveis = responsaveis;
    }
}
