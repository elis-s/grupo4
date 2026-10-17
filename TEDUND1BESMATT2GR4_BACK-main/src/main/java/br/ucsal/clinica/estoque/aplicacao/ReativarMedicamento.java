package br.ucsal.clinica.estoque.aplicacao;

import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.estoque.dominio.Medicamento;
import br.ucsal.clinica.estoque.dominio.MedicamentoEstoque;
import br.ucsal.clinica.estoque.porta.MedicamentoRepositoryPort;

/** Reativa um medicamento inativado. */
public class ReativarMedicamento {

    private final MedicamentoRepositoryPort medicamentos;

    public ReativarMedicamento(MedicamentoRepositoryPort medicamentos) {
        this.medicamentos = medicamentos;
    }

    public MedicamentoEstoque executar(Long id) {
        Medicamento medicamento = medicamentos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medicamento", id));
        medicamento.reativar();
        return new MedicamentoEstoque(medicamentos.salvar(medicamento), medicamentos.saldo(id));
    }
}
