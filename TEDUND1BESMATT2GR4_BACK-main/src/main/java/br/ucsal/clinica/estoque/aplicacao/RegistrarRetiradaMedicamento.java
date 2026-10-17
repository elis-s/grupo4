package br.ucsal.clinica.estoque.aplicacao;

import br.ucsal.clinica.autenticacao.dominio.UsuarioAutenticado;
import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.estoque.dominio.Medicamento;
import br.ucsal.clinica.estoque.dominio.MovimentacaoEstoque;
import br.ucsal.clinica.estoque.dominio.SolicitacaoMedicamento;
import br.ucsal.clinica.estoque.porta.MedicamentoRepositoryPort;
import br.ucsal.clinica.estoque.porta.MovimentacaoEstoqueRepositoryPort;
import br.ucsal.clinica.estoque.porta.SolicitacaoMedicamentoRepositoryPort;

/**
 * Caso de uso RegistrarRetiradaMedicamento (RF037 a RF040 - fluxo da secao 4.4):
 *  - so retira medicamento de uma solicitacao APROVADA (RF059);
 *  - registra o colaborador solicitante (RN013), a data e a hora (RN014) e o responsavel
 *    pela entrega, obtido do usuario autenticado - nunca enviado pelo Front-End (RN018);
 *  - nao permite retirar mais que o saldo disponivel (RN032);
 *  - informa o saldo atual e se o nivel minimo de 10 unidades foi atingido (RN015, RN016).
 * Executa numa unica transacao, com bloqueio otimista no estoque (RNF021, DA013).
 */
public class RegistrarRetiradaMedicamento {

    public record Comando(Long solicitacaoId, Long medicamentoId, Integer quantidade) {
    }

    private final SolicitacaoMedicamentoRepositoryPort solicitacoes;
    private final MedicamentoRepositoryPort medicamentos;
    private final MovimentacaoEstoqueRepositoryPort movimentacoes;
    private final SecurityContextPort contexto;

    public RegistrarRetiradaMedicamento(SolicitacaoMedicamentoRepositoryPort solicitacoes,
            MedicamentoRepositoryPort medicamentos, MovimentacaoEstoqueRepositoryPort movimentacoes,
            SecurityContextPort contexto) {
        this.solicitacoes = solicitacoes;
        this.medicamentos = medicamentos;
        this.movimentacoes = movimentacoes;
        this.contexto = contexto;
    }

    public RegistrarReposicao.Resultado executar(Comando comando) {
        SolicitacaoMedicamento solicitacao = solicitacoes.buscarPorId(comando.solicitacaoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação", comando.solicitacaoId()));
        Long medicamentoId = solicitacao.getMedicamento().id();
        if (comando.medicamentoId() != null && !comando.medicamentoId().equals(medicamentoId)) {
            throw new RegraNegocioException("O medicamento informado não é o da solicitação.");
        }
        int quantidade = comando.quantidade() == null ? solicitacao.getQuantidade() : comando.quantidade();
        if (quantidade > solicitacao.getQuantidade()) {
            throw new RegraNegocioException("A quantidade retirada (" + quantidade
                    + ") não pode ser maior que a quantidade aprovada (" + solicitacao.getQuantidade() + ").");
        }
        Medicamento medicamento = medicamentos.buscarPorId(medicamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medicamento", medicamentoId));
        if (!medicamento.isAtivo()) {
            throw new RegraNegocioException("O medicamento " + medicamento.getNome() + " está inativo.");
        }

        medicamentos.bloquearParaMovimentar(medicamentoId); // DA013
        long saldo = medicamentos.saldo(medicamentoId);
        if (quantidade > saldo) { // RN032
            throw new RegraNegocioException("Quantidade solicitada (" + quantidade
                    + ") maior que o saldo disponível (" + saldo + ").");
        }

        solicitacao.registrarEntrega();
        solicitacoes.salvar(solicitacao);

        UsuarioAutenticado responsavel = contexto.usuarioAtual(); // RN018
        MovimentacaoEstoque saida = movimentacoes.registrar(MovimentacaoEstoque.saida(
                new Referencia(medicamento.getId(), medicamento.getNome()), quantidade, solicitacao.getId(),
                solicitacao.getSolicitante(), new Referencia(responsavel.id(), responsavel.login()),
                "Retirada da solicitação " + solicitacao.getId()));

        long saldoAtual = saldo - quantidade;
        return new RegistrarReposicao.Resultado(saida, saldoAtual, saldoAtual <= medicamento.getEstoqueMinimo());
    }
}
