package br.ucsal.clinica.relatorios.aplicacao;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

import br.ucsal.clinica.autenticacao.dominio.UsuarioAutenticado;
import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.estoque.dominio.SituacaoSolicitacao;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaMedicamento;
import br.ucsal.clinica.relatorios.dominio.Relatorios.RelatorioUsuario;
import br.ucsal.clinica.relatorios.porta.RelatorioConsultaPort;

/**
 * Caso de uso EmitirRelatorioUsuario (RF060): informacoes cadastradas, o cadastramento dos
 * espacos (setores) e a aprovacao ou reprovacao das solicitacoes de medicamento.
 */
public class EmitirRelatorioUsuario {

    private final RelatorioConsultaPort consultas;
    private final SecurityContextPort contexto;

    public EmitirRelatorioUsuario(RelatorioConsultaPort consultas, SecurityContextPort contexto) {
        this.consultas = consultas;
        this.contexto = contexto;
    }

    public RelatorioUsuario executar() {
        UsuarioAutenticado usuario = contexto.usuarioAtual();
        var setores = consultas.setores();
        var pacientes = consultas.pacientes();
        var agendamentos = consultas.agendamentos(null);
        var medicamentos = consultas.medicamentos();
        var analisadas = consultas.solicitacoes(null,
                EnumSet.of(SituacaoSolicitacao.APROVADA, SituacaoSolicitacao.REPROVADA, SituacaoSolicitacao.ENTREGUE));
        var retiradas = consultas.movimentacoes(null, null, usuario.id());

        Map<String, Long> totais = new LinkedHashMap<>();
        totais.put("setores", (long) setores.size());
        totais.put("pacientes", (long) pacientes.size());
        totais.put("agendamentos", (long) agendamentos.size());
        totais.put("medicamentos", (long) medicamentos.size());
        totais.put("medicamentosParaRepor", medicamentos.stream().filter(LinhaMedicamento::reposicaoNecessaria).count());
        totais.put("solicitacoesAprovadas", analisadas.stream()
                .filter(s -> !"REPROVADA".equals(s.situacao())).count());
        totais.put("solicitacoesReprovadas", analisadas.stream()
                .filter(s -> "REPROVADA".equals(s.situacao())).count());
        totais.put("retiradasRealizadasPorMim", (long) retiradas.size());

        return new RelatorioUsuario(EmitirRelatorioAdministrativoCompleto.emissor(usuario), totais, setores, pacientes,
                agendamentos, medicamentos, analisadas, retiradas);
    }
}
