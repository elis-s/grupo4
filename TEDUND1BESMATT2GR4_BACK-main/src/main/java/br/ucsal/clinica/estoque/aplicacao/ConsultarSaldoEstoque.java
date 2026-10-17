package br.ucsal.clinica.estoque.aplicacao;

import java.util.List;

import br.ucsal.clinica.estoque.dominio.SaldoEstoque;
import br.ucsal.clinica.estoque.porta.MedicamentoRepositoryPort;

/** Caso de uso ConsultarSaldoEstoque (RF035, RF057): saldo atual de cada medicamento ativo. */
public class ConsultarSaldoEstoque {

    private final MedicamentoRepositoryPort medicamentos;

    public ConsultarSaldoEstoque(MedicamentoRepositoryPort medicamentos) {
        this.medicamentos = medicamentos;
    }

    public List<SaldoEstoque> executar() {
        return medicamentos.saldos();
    }
}
