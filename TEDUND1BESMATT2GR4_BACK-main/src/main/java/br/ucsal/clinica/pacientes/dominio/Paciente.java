package br.ucsal.clinica.pacientes.dominio;

import java.time.LocalDate;

import br.ucsal.clinica.compartilhado.dominio.EntidadeDominio;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.compartilhado.dominio.Textos;

/**
 * Paciente (RF027, RF029, RF049, RF050). Todo paciente tem exatamente um prontuario,
 * criado junto com ele (RN007 a RN009, DA012). Pode ser inativado quando houver
 * restricao para nao ser mais atendido, mantendo o historico (RN011).
 */
public class Paciente extends EntidadeDominio {

    private String nomeCompleto;
    private String cpf;
    private LocalDate dataNascimento;
    private String sexo;
    private String telefone;
    private String email;
    private Referencia medico;
    private Referencia especialidade;
    private Referencia convenio;
    private String numeroCarteirinha;
    private Referencia prontuario;

    /** RF027 / RF052: dados de identificacao, medico, especialidade e, se houver, convenio com carteirinha. */
    public void validar() {
        nomeCompleto = Textos.obrigatorio(nomeCompleto, "Informe o nome completo do paciente.");
        cpf = Textos.vazioParaNulo(cpf);
        if (dataNascimento == null || dataNascimento.isAfter(LocalDate.now())) {
            throw new RegraNegocioException("Informe uma data de nascimento válida.");
        }
        if (medico == null || medico.id() == null) {
            throw new RegraNegocioException("Informe o médico do paciente.");
        }
        if (especialidade == null || especialidade.id() == null) {
            throw new RegraNegocioException("Informe a especialidade médica.");
        }
        if (convenio == null || convenio.id() == null) {
            convenio = null;
            numeroCarteirinha = null;
        } else if (Textos.vazio(numeroCarteirinha)) {
            throw new RegraNegocioException("Informe o número da carteirinha do convênio.");
        }
    }

    /** RN011: o paciente com restricao pode ser inativado, mantendo o historico. */
    public void garantirAtivoParaAtendimento() {
        if (!isAtivo()) {
            throw new RegraNegocioException("O paciente " + nomeCompleto
                    + " está inativo (restrição de atendimento): " + getMotivoInativacao());
        }
    }

    /** RF049: edicao dos dados do paciente. */
    public void atualizarCom(Paciente dados) {
        this.nomeCompleto = dados.getNomeCompleto();
        this.cpf = dados.getCpf();
        this.dataNascimento = dados.getDataNascimento();
        this.sexo = dados.getSexo();
        this.telefone = dados.getTelefone();
        this.email = dados.getEmail();
        this.medico = dados.getMedico();
        this.especialidade = dados.getEspecialidade();
        this.convenio = dados.getConvenio();
        this.numeroCarteirinha = dados.getNumeroCarteirinha();
    }

    // ---------------- getters e setters ----------------

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Referencia getMedico() {
        return medico;
    }

    public void setMedico(Referencia medico) {
        this.medico = medico;
    }

    public Referencia getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(Referencia especialidade) {
        this.especialidade = especialidade;
    }

    public Referencia getConvenio() {
        return convenio;
    }

    public void setConvenio(Referencia convenio) {
        this.convenio = convenio;
    }

    public String getNumeroCarteirinha() {
        return numeroCarteirinha;
    }

    public void setNumeroCarteirinha(String numeroCarteirinha) {
        this.numeroCarteirinha = numeroCarteirinha;
    }

    public Referencia getProntuario() {
        return prontuario;
    }

    public void setProntuario(Referencia prontuario) {
        this.prontuario = prontuario;
    }
}
