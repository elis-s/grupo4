package br.ucsal.clinica.atendimento.aplicacao;

import java.util.List;

import br.ucsal.clinica.atendimento.dominio.AtendimentoClinico;
import br.ucsal.clinica.atendimento.dominio.PrescricaoMedica;
import br.ucsal.clinica.atendimento.porta.AtendimentoRepositoryPort;
import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.estoque.dominio.Medicamento;
import br.ucsal.clinica.estoque.porta.MedicamentoRepositoryPort;

/** Caso de uso EmitirPrescricaoMedica (RF020): medicamentos utilizados ou prescritos, com a dosagem. */
public class EmitirPrescricaoMedica {

    public record Item(Long medicamentoId, String dosagem, String posologia) {
    }

    private final AtendimentoRepositoryPort atendimentos;
    private final MedicamentoRepositoryPort medicamentos;
    private final SecurityContextPort contexto;

    public EmitirPrescricaoMedica(AtendimentoRepositoryPort atendimentos, MedicamentoRepositoryPort medicamentos,
            SecurityContextPort contexto) {
        this.atendimentos = atendimentos;
        this.medicamentos = medicamentos;
        this.contexto = contexto;
    }

    public AtendimentoClinico executar(Long atendimentoId, List<Item> itens) {
        AtendimentoClinico atendimento = atendimentos.buscarPorId(atendimentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atendimento", atendimentoId));
        atendimento.garantirResponsavel(RealizarTriagemPaciente.medicoLogado(contexto).colaboradorId());
        if (itens == null || itens.isEmpty()) {
            throw new RegraNegocioException("Informe ao menos um medicamento.");
        }
        for (Item item : itens) {
            Medicamento medicamento = medicamentos.buscarPorId(item.medicamentoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Medicamento", item.medicamentoId()));
            if (!medicamento.isAtivo()) {
                throw new RegraNegocioException("O medicamento " + medicamento.getNome() + " está inativo.");
            }
            atendimento.prescrever(new PrescricaoMedica(null,
                    new Referencia(medicamento.getId(), medicamento.getNome()), item.dosagem(), item.posologia()));
        }
        return atendimentos.salvar(atendimento);
    }
}
