package br.ucsal.clinica.relatorios.dominio;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

/** Relatorios emitidos pelo sistema. Todos identificam o emissor (RN034). */
public final class Relatorios {

    private Relatorios() {
    }

    /** RF015 / RF061: relatorio completo do administrador, com todas as informacoes cadastradas. */
    public record RelatorioAdministrativo(
            EmissorRelatorio emissor,
            Map<String, Long> totais,
            List<LinhaClinica> clinicas,
            List<LinhaSetor> setores,
            List<LinhaConvenio> convenios,
            List<LinhaColaborador> colaboradores,
            List<LinhaUsuario> usuarios,
            List<LinhaPaciente> pacientes,
            List<LinhaAgendamento> agendamentos,
            List<LinhaAtendimento> atendimentos,
            List<LinhaMedicamento> medicamentos,
            List<LinhaSolicitacao> solicitacoes,
            List<LinhaMovimentacao> movimentacoes) {
    }

    /** RF024 / RN020: movimentacao do proprio colaborador (agenda, atendimentos e solicitacoes). */
    public record RelatorioMovimentacaoColaborador(
            EmissorRelatorio emissor,
            String colaborador,
            String funcao,
            Map<String, Long> totais,
            List<LinhaAgendamento> agenda,
            List<LinhaAtendimento> atendimentos,
            List<LinhaSolicitacao> solicitacoes) {
    }

    /**
     * RF060: relatorio do usuario (atendente) com as informacoes cadastradas, incluindo o
     * cadastramento dos espacos (setores) e a aprovacao ou reprovacao das solicitacoes.
     */
    public record RelatorioUsuario(
            EmissorRelatorio emissor,
            Map<String, Long> totais,
            List<LinhaSetor> setores,
            List<LinhaPaciente> pacientes,
            List<LinhaAgendamento> agendamentos,
            List<LinhaMedicamento> medicamentos,
            List<LinhaSolicitacao> solicitacoesAnalisadas,
            List<LinhaMovimentacao> retiradasRealizadas) {
    }

    /** RF043: boletim das movimentacoes de estoque e medicamentos no periodo. */
    public record BoletimMovimentacaoFarmacia(
            EmissorRelatorio emissor,
            LocalDate inicio,
            LocalDate fim,
            Map<String, Long> totais,
            List<LinhaMedicamento> saldos,
            List<LinhaMovimentacao> movimentacoes) {
    }
}
