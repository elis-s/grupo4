package br.ucsal.clinica.estoque.porta;

import java.util.List;
import java.util.Optional;

import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.Status;
import br.ucsal.clinica.estoque.dominio.Medicamento;
import br.ucsal.clinica.estoque.dominio.SaldoEstoque;

/** Porta de persistencia dos medicamentos e de consulta dos saldos. */
public interface MedicamentoRepositoryPort {

    Optional<Medicamento> buscarPorId(Long id);

    Medicamento salvar(Medicamento medicamento);

    Pagina<Medicamento> listar(Status status, Paginacao paginacao);

    boolean existeNomeFabricante(String nome, String fabricante, Long ignorarId);

    /** RF057: saldo atual (entradas - saidas). */
    long saldo(Long medicamentoId);

    /** RF057: saldo de todos os medicamentos ativos. */
    List<SaldoEstoque> saldos();

    /** RF058: medicamentos que atingiram a quantidade minima. */
    List<SaldoEstoque> abaixoDoMinimo();

    /**
     * RNF021 / DA013: bloqueio otimista do estoque do medicamento. A versao do medicamento
     * sobe a cada movimentacao; se outra movimentacao simultanea gravar antes, esta
     * transacao entra em conflito e e repetida com o saldo revalidado.
     */
    void bloquearParaMovimentar(Long medicamentoId);
}
