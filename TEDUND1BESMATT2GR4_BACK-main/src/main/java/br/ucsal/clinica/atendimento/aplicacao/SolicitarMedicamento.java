package br.ucsal.clinica.atendimento.aplicacao;

import br.ucsal.clinica.atendimento.dominio.AtendimentoClinico;
import br.ucsal.clinica.atendimento.porta.AtendimentoRepositoryPort;
import br.ucsal.clinica.autenticacao.dominio.UsuarioAutenticado;
import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.estoque.dominio.Medicamento;
import br.ucsal.clinica.estoque.dominio.SolicitacaoMedicamento;
import br.ucsal.clinica.estoque.porta.MedicamentoRepositoryPort;
import br.ucsal.clinica.estoque.porta.SolicitacaoMedicamentoRepositoryPort;

/**
 * Caso de uso SolicitarMedicamento (RF023, RF038, RN013, RN019): o colaborador registra a
 * solicitacao (medicamento, quantidade e justificativa), que nasce PENDENTE e e enviada ao
 * Modulo de Estoque e Farmacia para aprovacao ou reprovacao (RF059).
 */
public class SolicitarMedicamento {

    public record Comando(Long medicamentoId, int quantidade, String justificativa, Long atendimentoId) {
    }

    private final SolicitacaoMedicamentoRepositoryPort solicitacoes;
    private final MedicamentoRepositoryPort medicamentos;
    private final AtendimentoRepositoryPort atendimentos;
    private final SecurityContextPort contexto;

    public SolicitarMedicamento(SolicitacaoMedicamentoRepositoryPort solicitacoes,
            MedicamentoRepositoryPort medicamentos, AtendimentoRepositoryPort atendimentos,
            SecurityContextPort contexto) {
        this.solicitacoes = solicitacoes;
        this.medicamentos = medicamentos;
        this.atendimentos = atendimentos;
        this.contexto = contexto;
    }

    public SolicitacaoMedicamento executar(Comando comando) {
        UsuarioAutenticado colaborador = RealizarTriagemPaciente.colaboradorLogado(contexto); // RF038
        Medicamento medicamento = medicamentos.buscarPorId(comando.medicamentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medicamento", comando.medicamentoId()));
        if (!medicamento.isAtivo()) {
            throw new RegraNegocioException("O medicamento " + medicamento.getNome() + " está inativo.");
        }
        if (comando.atendimentoId() != null) {
            AtendimentoClinico atendimento = atendimentos.buscarPorId(comando.atendimentoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Atendimento", comando.atendimentoId()));
            atendimento.garantirResponsavel(colaborador.colaboradorId());
        }
        SolicitacaoMedicamento solicitacao = SolicitacaoMedicamento.nova(
                new Referencia(medicamento.getId(), medicamento.getNome()),
                new Referencia(colaborador.colaboradorId(), colaborador.nome()),
                comando.quantidade(), comando.justificativa(), comando.atendimentoId());
        return solicitacoes.salvar(solicitacao);
    }
}
