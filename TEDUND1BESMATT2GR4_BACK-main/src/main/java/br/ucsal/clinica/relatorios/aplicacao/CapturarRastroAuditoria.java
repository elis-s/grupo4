package br.ucsal.clinica.relatorios.aplicacao;

import br.ucsal.clinica.relatorios.dominio.RegistroAuditoria;
import br.ucsal.clinica.relatorios.porta.RegistroAuditoriaRepositoryPort;

/**
 * Caso de uso CapturarRastroAuditoria (RF046): grava na tabela imutavel tb_auditoria o
 * evento recebido dos demais modulos (o Modulo de Relatorios e Auditoria e um ouvinte passivo).
 */
public class CapturarRastroAuditoria {

    private final RegistroAuditoriaRepositoryPort registros;

    public CapturarRastroAuditoria(RegistroAuditoriaRepositoryPort registros) {
        this.registros = registros;
    }

    public void executar(RegistroAuditoria registro) {
        registros.registrar(registro);
    }
}
