package br.ucsal.clinica.relatorios.porta;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import br.ucsal.clinica.estoque.dominio.SituacaoSolicitacao;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaAgendamento;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaAtendimento;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaClinica;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaColaborador;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaConvenio;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaMedicamento;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaMovimentacao;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaPaciente;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaSetor;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaSolicitacao;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaUsuario;

/**
 * Consultas somente leitura usadas pelos relatorios (secao 3.2.6 - Read-Only Projections),
 * isoladas das portas transacionais dos outros modulos. Filtros nulos = todos.
 */
public interface RelatorioConsultaPort {

    List<LinhaClinica> clinicas();

    List<LinhaSetor> setores();

    List<LinhaConvenio> convenios();

    List<LinhaColaborador> colaboradores();

    List<LinhaUsuario> usuarios();

    List<LinhaPaciente> pacientes();

    List<LinhaAgendamento> agendamentos(Long medicoId);

    List<LinhaAtendimento> atendimentos(Long colaboradorId);

    List<LinhaMedicamento> medicamentos();

    List<LinhaSolicitacao> solicitacoes(Long solicitanteId, Set<SituacaoSolicitacao> situacoes);

    List<LinhaMovimentacao> movimentacoes(LocalDateTime inicio, LocalDateTime fim, Long responsavelEntregaId);
}
