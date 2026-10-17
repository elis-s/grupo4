package br.ucsal.clinica.seguranca;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.ucsal.clinica.autenticacao.aplicacao.LimparTokensExpirados;

/** Tarefa agendada: a cada hora remove de tb_token_revogado os tokens que ja expiraram. */
@Component
public class LimpezaTokensRevogados {

    private static final Logger log = LoggerFactory.getLogger(LimpezaTokensRevogados.class);

    private final LimparTokensExpirados limpar;

    public LimpezaTokensRevogados(LimparTokensExpirados limpar) {
        this.limpar = limpar;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void executar() {
        int removidos = limpar.executar();
        if (removidos > 0) {
            log.info("{} token(s) revogado(s) e já expirado(s) removido(s).", removidos);
        }
    }
}
