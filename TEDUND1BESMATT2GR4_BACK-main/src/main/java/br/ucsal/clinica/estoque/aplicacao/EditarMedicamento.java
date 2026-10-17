package br.ucsal.clinica.estoque.aplicacao;

import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.estoque.dominio.Medicamento;
import br.ucsal.clinica.estoque.dominio.MedicamentoEstoque;
import br.ucsal.clinica.estoque.porta.MedicamentoRepositoryPort;

/** Caso de uso EditarMedicamento (RF055, RF041 - quantidade minima). */
public class EditarMedicamento {

    private final MedicamentoRepositoryPort medicamentos;

    public EditarMedicamento(MedicamentoRepositoryPort medicamentos) {
        this.medicamentos = medicamentos;
    }

    public MedicamentoEstoque executar(Long id, Medicamento dados, Long versaoLida) {
        Medicamento medicamento = medicamentos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medicamento", id));
        medicamento.verificarVersao(versaoLida);
        dados.validar();
        if (medicamentos.existeNomeFabricante(dados.getNome(), dados.getFabricante(), id)) {
            throw new RegraNegocioException("Já existe outro medicamento com este nome e fabricante.");
        }
        medicamento.atualizarCom(dados);
        Medicamento salvo = medicamentos.salvar(medicamento);
        return new MedicamentoEstoque(salvo, medicamentos.saldo(id));
    }
}
