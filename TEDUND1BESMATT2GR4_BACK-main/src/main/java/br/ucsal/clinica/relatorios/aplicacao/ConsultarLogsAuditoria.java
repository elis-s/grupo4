package br.ucsal.clinica.relatorios.aplicacao;

import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.relatorios.dominio.FiltroAuditoria;
import br.ucsal.clinica.relatorios.dominio.RegistroAuditoria;
import br.ucsal.clinica.relatorios.porta.RegistroAuditoriaRepositoryPort;

/** Caso de uso ConsultarLogsAuditoria (RF014): consulta paginada, mais recentes primeiro (DA014). */
public class ConsultarLogsAuditoria {

    private final RegistroAuditoriaRepositoryPort registros;

    public ConsultarLogsAuditoria(RegistroAuditoriaRepositoryPort registros) {
        this.registros = registros;
    }

    public Pagina<RegistroAuditoria> executar(FiltroAuditoria filtro, Paginacao paginacao) {
        if (filtro.inicio() != null && filtro.fim() != null && filtro.fim().isBefore(filtro.inicio())) {
            throw new RegraNegocioException("A data final não pode ser anterior à data inicial.");
        }
        return registros.listar(filtro, paginacao);
    }
}
