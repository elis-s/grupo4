package br.ucsal.clinica.estoque.aplicacao;

import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.estoque.dominio.Medicamento;
import br.ucsal.clinica.estoque.dominio.MedicamentoEstoque;
import br.ucsal.clinica.estoque.porta.MedicamentoRepositoryPort;

/** Caso de uso InativarMedicamento (RF056, RN012): o historico de movimentacoes e mantido. */
public class InativarMedicamento {

    private final MedicamentoRepositoryPort medicamentos;

    public InativarMedicamento(MedicamentoRepositoryPort medicamentos) {
        this.medicamentos = medicamentos;
    }

    public MedicamentoEstoque executar(Long id, String motivo) {
        Medicamento medicamento = medicamentos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medicamento", id));
        medicamento.inativar(motivo);
        return new MedicamentoEstoque(medicamentos.salvar(medicamento), medicamentos.saldo(id));
    }
}
