package br.ucsal.clinica.relatorios.aplicacao;

import java.util.LinkedHashMap;
import java.util.Map;

import br.ucsal.clinica.autenticacao.dominio.UsuarioAutenticado;
import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.compartilhado.dominio.AcessoNegadoException;
import br.ucsal.clinica.relatorios.dominio.Relatorios.RelatorioMovimentacaoColaborador;
import br.ucsal.clinica.relatorios.porta.RelatorioConsultaPort;

/**
 * Caso de uso EmitirRelatorioMovimentacaoColaborador (RF024, RN020): o colaborador consulta
 * somente a propria movimentacao - o filtro e o usuario autenticado, nunca um parametro.
 */
public class EmitirRelatorioMovimentacaoColaborador {

    private final RelatorioConsultaPort consultas;
    private final SecurityContextPort contexto;

    public EmitirRelatorioMovimentacaoColaborador(RelatorioConsultaPort consultas, SecurityContextPort contexto) {
        this.consultas = consultas;
        this.contexto = contexto;
    }

    public RelatorioMovimentacaoColaborador executar() {
        UsuarioAutenticado usuario = contexto.usuarioAtual();
        Long colaboradorId = usuario.colaboradorId();
        if (colaboradorId == null) {
            throw new AcessoNegadoException("Seu usuário não está vinculado a um colaborador.");
        }
        var agenda = consultas.agendamentos(colaboradorId);
        var atendimentos = consultas.atendimentos(colaboradorId);
        var solicitacoes = consultas.solicitacoes(colaboradorId, null);

        Map<String, Long> totais = new LinkedHashMap<>();
        totais.put("agendamentos", (long) agenda.size());
        totais.put("atendimentos", (long) atendimentos.size());
        totais.put("atendimentosEmAndamento", atendimentos.stream().filter(a -> a.encerramento() == null).count());
        totais.put("internacoes", atendimentos.stream().filter(a -> a.internado()).count());
        totais.put("solicitacoesDeMedicamento", (long) solicitacoes.size());
        totais.put("solicitacoesAprovadasOuEntregues", solicitacoes.stream()
                .filter(s -> "APROVADA".equals(s.situacao()) || "ENTREGUE".equals(s.situacao())).count());
        totais.put("solicitacoesReprovadas",
                solicitacoes.stream().filter(s -> "REPROVADA".equals(s.situacao())).count());

        return new RelatorioMovimentacaoColaborador(EmitirRelatorioAdministrativoCompleto.emissor(usuario),
                usuario.nome(), usuario.funcao(), totais, agenda, atendimentos, solicitacoes);
    }
}
