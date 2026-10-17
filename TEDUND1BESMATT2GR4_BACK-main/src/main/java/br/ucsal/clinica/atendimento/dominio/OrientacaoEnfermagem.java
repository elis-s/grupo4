package br.ucsal.clinica.atendimento.dominio;

import java.time.LocalDateTime;

import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.Textos;

/**
 * Orientacao de enfermagem (RF022): registrada pelo colaborador quando o paciente esta
 * internado ou em atendimento que exige acompanhamento. Somente inclusao.
 */
public class OrientacaoEnfermagem {

    private Long id;
    private Long atendimentoId;
    private Referencia colaborador;
    private String orientacao;
    private LocalDateTime dataHora;

    public static OrientacaoEnfermagem nova(Long atendimentoId, Referencia colaborador, String texto) {
        OrientacaoEnfermagem o = new OrientacaoEnfermagem();
        o.atendimentoId = atendimentoId;
        o.colaborador = colaborador;
        o.orientacao = Textos.obrigatorio(texto, "Escreva a orientação para a enfermagem.");
        o.dataHora = LocalDateTime.now();
        return o;
    }

    // ---------------- getters e setters ----------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAtendimentoId() {
        return atendimentoId;
    }

    public void setAtendimentoId(Long atendimentoId) {
        this.atendimentoId = atendimentoId;
    }

    public Referencia getColaborador() {
        return colaborador;
    }

    public void setColaborador(Referencia colaborador) {
        this.colaborador = colaborador;
    }

    public String getOrientacao() {
        return orientacao;
    }

    public void setOrientacao(String orientacao) {
        this.orientacao = orientacao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}
