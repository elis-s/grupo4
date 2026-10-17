package br.ucsal.clinica.relatorios.porta;

import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.relatorios.dominio.FiltroAuditoria;
import br.ucsal.clinica.relatorios.dominio.RegistroAuditoria;

/** Porta da tabela append-only tb_auditoria (RN036): somente inclusao e consulta. */
public interface RegistroAuditoriaRepositoryPort {

    void registrar(RegistroAuditoria registro);

    Pagina<RegistroAuditoria> listar(FiltroAuditoria filtro, Paginacao paginacao);
}
