package br.ucsal.clinica.atendimento.dominio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.ucsal.clinica.compartilhado.dominio.AcessoNegadoException;
import br.ucsal.clinica.compartilhado.dominio.EntidadeDominio;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.compartilhado.dominio.Textos;

/**
 * Atendimento clinico registrado no prontuario (RF016 a RF021): paciente, colaborador,
 * tipo (RN027), data e hora de inicio, sintomas, diagnostico, tratamento, medicamentos com
 * dosagem e data e hora de encerramento. Maquina de estados: EM_TRIAGEM -> EM_ATENDIMENTO
 * -> FINALIZADO (secao 3.2.4).
 */
public class AtendimentoClinico extends EntidadeDominio {

    private Referencia prontuario;
    private Referencia paciente;
    private Referencia colaborador;
    private Long agendamentoId;
    private Referencia convenio;
    private TipoAtendimento tipo;
    private EtapaAtendimento etapa = EtapaAtendimento.EM_TRIAGEM;
    private LocalDateTime dataHoraInicio;
    private String sintomas;
    private String diagnostico;
    private String tratamento;
    private LocalDateTime dataHoraEncerramento;
    private boolean internado;
    private List<PrescricaoMedica> prescricoes = new ArrayList<>();

    /**
     * RealizarTriagemPaciente (RF016, RF017, RF021): abre o atendimento na etapa EM_TRIAGEM com o
     * tipo (RN027) e os sintomas. O colaborador responsavel vem do usuario autenticado (RN028).
     */
    public static AtendimentoClinico abrirTriagem(Referencia prontuario, Referencia paciente, Referencia colaborador,
            Referencia convenio, Long agendamentoId, TipoAtendimento tipo, String sintomas) {
        if (tipo == null) {
            throw new RegraNegocioException("Informe o tipo: URGENCIA, EMERGENCIA, CONSULTA ou REVISAO (RN027).");
        }
        if (colaborador == null || colaborador.id() == null) {
            throw new RegraNegocioException("O colaborador responsável é obtido do usuário autenticado (RN028).");
        }
        AtendimentoClinico a = new AtendimentoClinico();
        a.prontuario = prontuario;
        a.paciente = paciente;
        a.colaborador = colaborador;
        a.convenio = convenio;
        a.agendamentoId = agendamentoId;
        a.tipo = tipo;
        a.etapa = EtapaAtendimento.EM_TRIAGEM;
        a.dataHoraInicio = LocalDateTime.now();
        a.sintomas = Textos.obrigatorio(sintomas, "Registre os sintomas apresentados pelo paciente (RF017).");
        return a;
    }

    /** IniciarAtendimentoClinico: EM_TRIAGEM -> EM_ATENDIMENTO. */
    public void iniciarAtendimento() {
        garantirAtivo();
        if (etapa != EtapaAtendimento.EM_TRIAGEM) {
            throw new RegraNegocioException("O atendimento só pode ser iniciado a partir da triagem (etapa atual: "
                    + etapa + ").");
        }
        this.etapa = EtapaAtendimento.EM_ATENDIMENTO;
    }

    /** RF017 a RF019: sintomas, diagnostico e tratamento, enquanto o atendimento nao foi finalizado. */
    public void registrarEvolucao(String sintomas, String diagnostico, String tratamento) {
        garantirEmAndamento();
        this.sintomas = Textos.obrigatorio(sintomas, "Os sintomas são obrigatórios (RF017).");
        this.diagnostico = Textos.vazioParaNulo(diagnostico);
        this.tratamento = Textos.vazioParaNulo(tratamento);
    }

    /** EmitirPrescricaoMedica (RF020): medicamento e dosagem. */
    public void prescrever(PrescricaoMedica prescricao) {
        garantirEmAndamento();
        boolean repetido = prescricoes.stream()
                .anyMatch(p -> p.medicamento().id().equals(prescricao.medicamento().id()));
        if (repetido) {
            throw new RegraNegocioException("O medicamento " + prescricao.medicamento().nome()
                    + " já foi prescrito neste atendimento.");
        }
        prescricoes.add(prescricao);
    }

    /**
     * FinalizarAtendimento (RF032, RN010): EM_ATENDIMENTO -> FINALIZADO, com diagnostico.
     * Depois de finalizado, o registro clinico nao muda mais (historico imutavel do PEP).
     */
    public void finalizar(String diagnostico, String tratamento, boolean internado) {
        garantirAtivo();
        if (etapa != EtapaAtendimento.EM_ATENDIMENTO) {
            throw new RegraNegocioException("Só é possível finalizar um atendimento em andamento (etapa atual: "
                    + etapa + ").");
        }
        if (!Textos.vazio(diagnostico)) {
            this.diagnostico = diagnostico.trim();
        }
        if (!Textos.vazio(tratamento)) {
            this.tratamento = tratamento.trim();
        }
        if (Textos.vazio(this.diagnostico)) {
            throw new RegraNegocioException("Registre o diagnóstico antes de finalizar o atendimento (RF018).");
        }
        this.internado = internado;
        this.etapa = EtapaAtendimento.FINALIZADO;
        this.dataHoraEncerramento = LocalDateTime.now();
    }

    public boolean isFinalizado() {
        return etapa == EtapaAtendimento.FINALIZADO;
    }

    /** Autorizacao por dado: so o colaborador responsavel altera o atendimento. */
    public void garantirResponsavel(Long colaboradorId) {
        if (colaboradorId == null || !colaboradorId.equals(colaborador.id())) {
            throw new AcessoNegadoException("Somente o colaborador responsável pode alterar este atendimento.");
        }
    }

    /** RF022: orientacoes de enfermagem quando o paciente esta internado ou ainda em acompanhamento. */
    public boolean permiteOrientacaoEnfermagem() {
        return isAtivo() && (!isFinalizado() || internado);
    }

    private void garantirEmAndamento() {
        garantirAtivo();
        if (isFinalizado()) {
            throw new RegraNegocioException("Atendimento finalizado não pode ser alterado.");
        }
    }

    private void garantirAtivo() {
        if (!isAtivo()) {
            throw new RegraNegocioException("Este atendimento está inativo.");
        }
    }

    // ---------------- getters e setters ----------------

    public Referencia getProntuario() {
        return prontuario;
    }

    public void setProntuario(Referencia prontuario) {
        this.prontuario = prontuario;
    }

    public Referencia getPaciente() {
        return paciente;
    }

    public void setPaciente(Referencia paciente) {
        this.paciente = paciente;
    }

    public Referencia getColaborador() {
        return colaborador;
    }

    public void setColaborador(Referencia colaborador) {
        this.colaborador = colaborador;
    }

    public Long getAgendamentoId() {
        return agendamentoId;
    }

    public void setAgendamentoId(Long agendamentoId) {
        this.agendamentoId = agendamentoId;
    }

    public Referencia getConvenio() {
        return convenio;
    }

    public void setConvenio(Referencia convenio) {
        this.convenio = convenio;
    }

    public TipoAtendimento getTipo() {
        return tipo;
    }

    public void setTipo(TipoAtendimento tipo) {
        this.tipo = tipo;
    }

    public EtapaAtendimento getEtapa() {
        return etapa;
    }

    public void setEtapa(EtapaAtendimento etapa) {
        this.etapa = etapa;
    }

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public void setDataHoraInicio(LocalDateTime dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
    }

    public String getSintomas() {
        return sintomas;
    }

    public void setSintomas(String sintomas) {
        this.sintomas = sintomas;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getTratamento() {
        return tratamento;
    }

    public void setTratamento(String tratamento) {
        this.tratamento = tratamento;
    }

    public LocalDateTime getDataHoraEncerramento() {
        return dataHoraEncerramento;
    }

    public void setDataHoraEncerramento(LocalDateTime dataHoraEncerramento) {
        this.dataHoraEncerramento = dataHoraEncerramento;
    }

    public boolean isInternado() {
        return internado;
    }

    public void setInternado(boolean internado) {
        this.internado = internado;
    }

    public List<PrescricaoMedica> getPrescricoes() {
        return prescricoes;
    }

    public void setPrescricoes(List<PrescricaoMedica> prescricoes) {
        this.prescricoes = prescricoes;
    }
}
