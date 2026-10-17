package br.ucsal.clinica.atendimento.dominio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import br.ucsal.clinica.compartilhado.dominio.EntidadeDominio;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;

/**
 * Agendamento de consulta (RF025, RF026, RF032, RF053, RF054): inicia AGENDADO e
 * muda para FINALIZADO apos o atendimento (RN010). Pode ser inativado quando existir
 * restricao, mas nunca excluido (RN030).
 */
public class Agendamento extends EntidadeDominio {

    private Referencia medico;
    private Referencia paciente;
    private LocalDate dataPrevista;
    private LocalTime horaPrevista;
    private SituacaoAgendamento situacao = SituacaoAgendamento.AGENDADO;
    private String observacao;
    private LocalDateTime dataRecepcao;

    /** RF025: colaborador (medico), paciente, data e hora agendadas. */
    public void validar() {
        if (medico == null || medico.id() == null) {
            throw new RegraNegocioException("Informe o médico do agendamento.");
        }
        if (paciente == null || paciente.id() == null) {
            throw new RegraNegocioException("Informe o paciente do agendamento.");
        }
        if (dataPrevista == null || horaPrevista == null) {
            throw new RegraNegocioException("Informe a data e a hora da consulta.");
        }
    }

    public boolean isFinalizado() {
        return situacao == SituacaoAgendamento.FINALIZADO;
    }

    /** RF053: agendamento finalizado ou inativo nao pode ser alterado. */
    public void garantirEditavel() {
        if (isFinalizado()) {
            throw new RegraNegocioException("Agendamento finalizado não pode ser alterado.");
        }
        if (!isAtivo()) {
            throw new RegraNegocioException("Agendamento inativo não pode ser alterado.");
        }
    }

    /** RN010: apos o atendimento o agendamento passa para FINALIZADO. */
    public void finalizar() {
        this.situacao = SituacaoAgendamento.FINALIZADO;
    }

    /** RF053: edicao do agendamento. */
    public void atualizarCom(Agendamento dados) {
        this.medico = dados.getMedico();
        this.paciente = dados.getPaciente();
        this.dataPrevista = dados.getDataPrevista();
        this.horaPrevista = dados.getHoraPrevista();
        this.observacao = dados.getObservacao();
    }

    // ---------------- getters e setters ----------------

    public Referencia getMedico() {
        return medico;
    }

    public void setMedico(Referencia medico) {
        this.medico = medico;
    }

    public Referencia getPaciente() {
        return paciente;
    }

    public void setPaciente(Referencia paciente) {
        this.paciente = paciente;
    }

    public LocalDate getDataPrevista() {
        return dataPrevista;
    }

    public void setDataPrevista(LocalDate dataPrevista) {
        this.dataPrevista = dataPrevista;
    }

    public LocalTime getHoraPrevista() {
        return horaPrevista;
    }

    public void setHoraPrevista(LocalTime horaPrevista) {
        this.horaPrevista = horaPrevista;
    }

    public SituacaoAgendamento getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoAgendamento situacao) {
        this.situacao = situacao;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public LocalDateTime getDataRecepcao() {
        return dataRecepcao;
    }

    public void setDataRecepcao(LocalDateTime dataRecepcao) {
        this.dataRecepcao = dataRecepcao;
    }
}
