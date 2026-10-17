package br.ucsal.clinica.compartilhado.dominio;

import java.time.LocalDateTime;

/**
 * Base das entidades de dominio (Java puro, sem anotacoes de framework - secao 3.1.3).
 *
 * Concentra as regras comuns a todos os cadastros:
 *  - data de cadastro e status (exigidos em todos os cadastros do cenario);
 *  - inativacao logica com motivo, preservando o historico (RN002, RN003, RN022, DA011);
 *  - versao do registro, usada no bloqueio otimista (RNF021, DA013).
 */
public abstract class EntidadeDominio {

    private Long id;
    private Long versao;
    private LocalDateTime dataCadastro;
    private Status status = Status.ATIVO;
    private String motivoInativacao;
    private LocalDateTime dataInativacao;

    public boolean isAtivo() {
        return status == Status.ATIVO;
    }

    public boolean isNovo() {
        return id == null;
    }

    /**
     * Bloqueio otimista: o Front-End devolve a versao que leu. Se outra pessoa salvou
     * nesse meio tempo, a versao mudou e a edicao e recusada (409), em vez de
     * sobrescrever o trabalho do outro usuario.
     */
    public void verificarVersao(Long versaoLida) {
        if (versaoLida != null && versao != null && !versaoLida.equals(versao)) {
            throw new ConflitoVersaoException();
        }
    }

    /** RN003 / RN022 / RN023: inativa mantendo as informacoes e registrando data e motivo. */
    public void inativar(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new RegraNegocioException("Informe o motivo da inativação.");
        }
        if (!isAtivo()) {
            throw new RegraNegocioException("O registro já está inativo.");
        }
        this.status = Status.INATIVO;
        this.motivoInativacao = motivo.trim();
        this.dataInativacao = LocalDateTime.now();
    }

    public void reativar() {
        if (isAtivo()) {
            throw new RegraNegocioException("O registro já está ativo.");
        }
        this.status = Status.ATIVO;
        this.motivoInativacao = null;
        this.dataInativacao = null;
    }

    /** Usado pelos mapeadores da camada de persistencia para reconstruir a entidade. */
    public void restaurarControle(Long id, Long versao, LocalDateTime dataCadastro, Status status,
            String motivoInativacao, LocalDateTime dataInativacao) {
        this.id = id;
        this.versao = versao;
        this.dataCadastro = dataCadastro;
        this.status = status;
        this.motivoInativacao = motivoInativacao;
        this.dataInativacao = dataInativacao;
    }

    public Long getId() {
        return id;
    }

    public Long getVersao() {
        return versao;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public Status getStatus() {
        return status;
    }

    public String getMotivoInativacao() {
        return motivoInativacao;
    }

    public LocalDateTime getDataInativacao() {
        return dataInativacao;
    }
}
