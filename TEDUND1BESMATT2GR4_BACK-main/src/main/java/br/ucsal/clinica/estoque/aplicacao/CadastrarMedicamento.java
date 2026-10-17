package br.ucsal.clinica.estoque.aplicacao;

import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.estoque.dominio.EstoqueInicial;
import br.ucsal.clinica.estoque.dominio.Medicamento;
import br.ucsal.clinica.estoque.dominio.MedicamentoEstoque;
import br.ucsal.clinica.estoque.dominio.MovimentacaoEstoque;
import br.ucsal.clinica.estoque.porta.MedicamentoRepositoryPort;
import br.ucsal.clinica.estoque.porta.MovimentacaoEstoqueRepositoryPort;

/**
 * Caso de uso CadastrarMedicamento (RF033): a quantidade em estoque, a validade e a data
 * de aquisicao informadas no cadastro viram a primeira movimentacao de entrada.
 */
public class CadastrarMedicamento {

    private final MedicamentoRepositoryPort medicamentos;
    private final MovimentacaoEstoqueRepositoryPort movimentacoes;

    public CadastrarMedicamento(MedicamentoRepositoryPort medicamentos, MovimentacaoEstoqueRepositoryPort movimentacoes) {
        this.medicamentos = medicamentos;
        this.movimentacoes = movimentacoes;
    }

    public MedicamentoEstoque executar(Medicamento novo, EstoqueInicial estoqueInicial) {
        novo.validar();
        if (medicamentos.existeNomeFabricante(novo.getNome(), novo.getFabricante(), null)) {
            throw new RegraNegocioException("Este medicamento já está cadastrado para este fabricante.");
        }
        Medicamento salvo = medicamentos.salvar(novo);
        if (estoqueInicial != null && estoqueInicial.quantidade() > 0) {
            movimentacoes.registrar(MovimentacaoEstoque.entrada(new Referencia(salvo.getId(), salvo.getNome()),
                    estoqueInicial.quantidade(), estoqueInicial.lote(), estoqueInicial.dataValidade(),
                    estoqueInicial.dataAquisicao(), "Estoque inicial no cadastro"));
        }
        return new MedicamentoEstoque(salvo, medicamentos.saldo(salvo.getId()));
    }
}
