package br.ucsal.clinica.relatorios.aplicacao;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.ucsal.clinica.autenticacao.dominio.UsuarioAutenticado;
import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.compartilhado.dominio.AcessoNegadoException;
import br.ucsal.clinica.relatorios.dominio.EmissorRelatorio;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaMedicamento;
import br.ucsal.clinica.relatorios.dominio.Relatorios.RelatorioAdministrativo;
import br.ucsal.clinica.relatorios.porta.RelatorioConsultaPort;

/**
 * Caso de uso EmitirRelatorioAdministrativoCompleto (RF015, RF061, RN034): todas as
 * informacoes cadastradas, com a identificacao do administrador responsavel pela emissao.
 */
public class EmitirRelatorioAdministrativoCompleto {

    private final RelatorioConsultaPort consultas;
    private final SecurityContextPort contexto;

    public EmitirRelatorioAdministrativoCompleto(RelatorioConsultaPort consultas, SecurityContextPort contexto) {
        this.consultas = consultas;
        this.contexto = contexto;
    }

    public RelatorioAdministrativo executar() {
        UsuarioAutenticado usuario = contexto.usuarioAtual();
        if (!usuario.isAdministrador()) {
            throw new AcessoNegadoException("Somente o administrador emite o relatório completo.");
        }
        var clinicas = consultas.clinicas();
        var setores = consultas.setores();
        var convenios = consultas.convenios();
        var colaboradores = consultas.colaboradores();
        var usuarios = consultas.usuarios();
        var pacientes = consultas.pacientes();
        var agendamentos = consultas.agendamentos(null);
        var atendimentos = consultas.atendimentos(null);
        List<LinhaMedicamento> medicamentos = consultas.medicamentos();
        var solicitacoes = consultas.solicitacoes(null, null);
        var movimentacoes = consultas.movimentacoes(null, null, null);

        Map<String, Long> totais = new LinkedHashMap<>();
        totais.put("clinicas", (long) clinicas.size());
        totais.put("setores", (long) setores.size());
        totais.put("convenios", (long) convenios.size());
        totais.put("colaboradores", (long) colaboradores.size());
        totais.put("usuarios", (long) usuarios.size());
        totais.put("pacientes", (long) pacientes.size());
        totais.put("agendamentos", (long) agendamentos.size());
        totais.put("atendimentos", (long) atendimentos.size());
        totais.put("medicamentos", (long) medicamentos.size());
        totais.put("medicamentosParaRepor", medicamentos.stream().filter(LinhaMedicamento::reposicaoNecessaria).count());
        totais.put("solicitacoes", (long) solicitacoes.size());
        totais.put("movimentacoesDeEstoque", (long) movimentacoes.size());

        return new RelatorioAdministrativo(emissor(usuario), totais, clinicas, setores, convenios, colaboradores,
                usuarios, pacientes, agendamentos, atendimentos, medicamentos, solicitacoes, movimentacoes);
    }

    /** RN034: quem emitiu e quando. */
    static EmissorRelatorio emissor(UsuarioAutenticado usuario) {
        return new EmissorRelatorio(usuario.login(), usuario.nome(),
                usuario.perfis().stream().map(Enum::name).sorted().toList(), LocalDateTime.now());
    }
}
