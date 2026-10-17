package br.ucsal.clinica.relatorios.porta;

import br.ucsal.clinica.relatorios.dominio.AcaoAuditoria;
import br.ucsal.clinica.relatorios.dominio.ResultadoAuditoria;

/**
 * Porta pela qual os casos de uso e os adaptadores enviam eventos de seguranca ao
 * Modulo de Relatorios e Auditoria (login, logout, acesso negado...), sem depender
 * de como a gravacao e feita. Cadastros, edicoes e mudancas de status sao
 * capturados automaticamente pelos ouvintes de entidade JPA (secao 3.4.3).
 */
public interface RastroAuditoriaPort {

    /** Registra em nome do usuario autenticado na requisicao atual. */
    void registrar(AcaoAuditoria acao, ResultadoAuditoria resultado, String entidade, Long entidadeId,
            String descricao);

    /** Registra informando o login (ex.: tentativa de login, quando ainda nao ha usuario autenticado). */
    void registrarComo(String login, AcaoAuditoria acao, ResultadoAuditoria resultado, String entidade,
            Long entidadeId, String descricao);
}
