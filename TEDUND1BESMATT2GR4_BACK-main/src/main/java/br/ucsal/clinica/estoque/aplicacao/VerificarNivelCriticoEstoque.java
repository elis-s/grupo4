package br.ucsal.clinica.estoque.aplicacao;

import java.util.List;

import br.ucsal.clinica.estoque.dominio.SaldoEstoque;
import br.ucsal.clinica.estoque.porta.MedicamentoRepositoryPort;

/**
 * Caso de uso VerificarNivelCriticoEstoque (RF041, RF058, RN015, RN016): medicamentos
 * cujo saldo atingiu a quantidade minima (10 unidades) e precisam de reposicao.
 */
public class VerificarNivelCriticoEstoque {

    private final MedicamentoRepositoryPort medicamentos;

    public VerificarNivelCriticoEstoque(MedicamentoRepositoryPort medicamentos) {
        this.medicamentos = medicamentos;
    }

    public List<SaldoEstoque> executar() {
        return medicamentos.abaixoDoMinimo();
    }
}
