package br.ucsal.clinica.administracao.dominio;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import br.ucsal.clinica.compartilhado.dominio.EntidadeDominio;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.compartilhado.dominio.Textos;

/**
 * Colaborador: todo o corpo da clinica (RF010 a RF012, RF047, RF048).
 * Pode ser inativado quando nao houver mais vinculo com a IES, mas nunca excluido,
 * mantendo o historico de atendimentos e atividades (RN006, RN025).
 */
public class Colaborador extends EntidadeDominio {

    private String nomeCompleto;
    private String cpf;
    private String email;
    private String telefone;
    private Funcao funcao;
    private Referencia setor;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private Integer cargaHorariaSemanal;
    private String conselhoSigla;
    private String conselhoUf;
    private String conselhoNumero;
    private Referencia especialidade;
    private List<HorarioAtendimento> horariosAtendimento = new ArrayList<>();

    public boolean isMedico() {
        return funcao == Funcao.MEDICO;
    }

    /**
     * RF010 / RN024: o colaborador trabalha em um setor (local de trabalho) e o medico
     * precisa ter especialidade, conselho regional, numero de registro e dias/turnos.
     */
    public void validar() {
        if (setor == null || setor.id() == null) {
            throw new RegraNegocioException("Informe o local de trabalho (setor) do colaborador.");
        }
        conselhoSigla = Textos.vazioParaNulo(conselhoSigla);
        conselhoUf = Textos.vazioParaNulo(conselhoUf);
        conselhoNumero = Textos.vazioParaNulo(conselhoNumero);
        if (horariosAtendimento == null) {
            horariosAtendimento = new ArrayList<>();
        }
        horariosAtendimento = new ArrayList<>(new LinkedHashSet<>(horariosAtendimento));
        if (isMedico()) {
            if (especialidade == null || especialidade.id() == null) {
                throw new RegraNegocioException("Informe a especialidade do médico (RN024).");
            }
            if (conselhoSigla == null || conselhoUf == null || conselhoNumero == null) {
                throw new RegraNegocioException(
                        "Informe o conselho regional (sigla e UF) e o número de registro do médico (RN024).");
            }
            if (horariosAtendimento.isEmpty()) {
                throw new RegraNegocioException("Informe os dias e turnos de atendimento do médico (RN024).");
            }
        }
    }

    /** RF011: edicao do colaborador. */
    public void atualizarCom(Colaborador dados) {
        this.nomeCompleto = dados.getNomeCompleto();
        this.cpf = dados.getCpf();
        this.email = dados.getEmail();
        this.telefone = dados.getTelefone();
        this.funcao = dados.getFuncao();
        this.setor = dados.getSetor();
        this.horaInicio = dados.getHoraInicio();
        this.horaFim = dados.getHoraFim();
        this.cargaHorariaSemanal = dados.getCargaHorariaSemanal();
        this.conselhoSigla = dados.getConselhoSigla();
        this.conselhoUf = dados.getConselhoUf();
        this.conselhoNumero = dados.getConselhoNumero();
        this.especialidade = dados.getEspecialidade();
        this.horariosAtendimento = dados.getHorariosAtendimento();
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

    public Funcao getFuncao() {
        return funcao;
    }

    public void setFuncao(Funcao funcao) {
        this.funcao = funcao;
    }

    public Referencia getSetor() {
        return setor;
    }

    public void setSetor(Referencia setor) {
        this.setor = setor;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(LocalTime horaFim) {
        this.horaFim = horaFim;
    }

    public Integer getCargaHorariaSemanal() {
        return cargaHorariaSemanal;
    }

    public void setCargaHorariaSemanal(Integer cargaHorariaSemanal) {
        this.cargaHorariaSemanal = cargaHorariaSemanal;
    }

    public String getConselhoSigla() {
        return conselhoSigla;
    }

    public void setConselhoSigla(String conselhoSigla) {
        this.conselhoSigla = conselhoSigla;
    }

    public String getConselhoUf() {
        return conselhoUf;
    }

    public void setConselhoUf(String conselhoUf) {
        this.conselhoUf = conselhoUf;
    }

    public String getConselhoNumero() {
        return conselhoNumero;
    }

    public void setConselhoNumero(String conselhoNumero) {
        this.conselhoNumero = conselhoNumero;
    }

    public Referencia getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(Referencia especialidade) {
        this.especialidade = especialidade;
    }

    public List<HorarioAtendimento> getHorariosAtendimento() {
        return horariosAtendimento;
    }

    public void setHorariosAtendimento(List<HorarioAtendimento> horariosAtendimento) {
        this.horariosAtendimento = horariosAtendimento;
    }
}
