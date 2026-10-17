package br.ucsal.clinica.estoque.aplicacao;

import java.time.LocalDate;

import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.estoque.dominio.Medicamento;
import br.ucsal.clinica.estoque.dominio.MovimentacaoEstoque;
import br.ucsal.clinica.estoque.porta.MedicamentoRepositoryPort;
import br.ucsal.clinica.estoque.porta.MovimentacaoEstoqueRepositoryPort;

/**
 * Caso de uso RegistrarReposicao (RF042, RN017, RN031): toda reposicao e uma movimentacao
 * de ENTRADA e pode ser feita a qualquer tempo, antes ou depois de atingir o minimo.
 */
public class RegistrarReposicao {

    public record Comando(Long medicamentoId, int quantidade, String lote, LocalDate dataValidade,
            LocalDate dataAquisicao, String observacao) {
    }

    /** Movimentacao gravada, o saldo depois dela e se ainda e preciso repor (RF058). */
    public record Resultado(MovimentacaoEstoque movimentacao, long saldoAtual, boolean reposicaoNecessaria) {
    }

    private final MedicamentoRepositoryPort medicamentos;
    private final MovimentacaoEstoqueRepositoryPort movimentacoes;

    public RegistrarReposicao(MedicamentoRepositoryPort medicamentos, MovimentacaoEstoqueRepositoryPort movimentacoes) {
        this.medicamentos = medicamentos;
        this.movimentacoes = movimentacoes;
    }

    public Resultado executar(Comando comando) {
        Medicamento medicamento = medicamentos.buscarPorId(comando.medicamentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medicamento", comando.medicamentoId()));
        if (!medicamento.isAtivo()) {
            throw new RegraNegocioException("O medicamento " + medicamento.getNome() + " está inativo.");
        }
        medicamentos.bloquearParaMovimentar(medicamento.getId());
        MovimentacaoEstoque entrada = movimentacoes.registrar(MovimentacaoEstoque.entrada(
                new Referencia(medicamento.getId(), medicamento.getNome()), comando.quantidade(), comando.lote(),
                comando.dataValidade(), comando.dataAquisicao(), comando.observacao()));
        long saldo = medicamentos.saldo(medicamento.getId());
        return new Resultado(entrada, saldo, saldo <= medicamento.getEstoqueMinimo());
    }
}
