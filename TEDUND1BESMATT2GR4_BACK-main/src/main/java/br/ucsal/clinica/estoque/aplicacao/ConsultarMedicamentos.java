package br.ucsal.clinica.estoque.aplicacao;

import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Status;
import br.ucsal.clinica.estoque.dominio.Medicamento;
import br.ucsal.clinica.estoque.dominio.MedicamentoEstoque;
import br.ucsal.clinica.estoque.porta.MedicamentoRepositoryPort;

/** Caso de uso de consulta dos medicamentos e suas informacoes, com o saldo atual (RF034, RF057). */
public class ConsultarMedicamentos {

    private final MedicamentoRepositoryPort medicamentos;

    public ConsultarMedicamentos(MedicamentoRepositoryPort medicamentos) {
        this.medicamentos = medicamentos;
    }

    public MedicamentoEstoque porId(Long id) {
        Medicamento medicamento = medicamentos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medicamento", id));
        return new MedicamentoEstoque(medicamento, medicamentos.saldo(id));
    }

    public Pagina<MedicamentoEstoque> listar(Status status, Paginacao paginacao) {
        return medicamentos.listar(status, paginacao)
                .map(m -> new MedicamentoEstoque(m, medicamentos.saldo(m.getId())));
    }
}
