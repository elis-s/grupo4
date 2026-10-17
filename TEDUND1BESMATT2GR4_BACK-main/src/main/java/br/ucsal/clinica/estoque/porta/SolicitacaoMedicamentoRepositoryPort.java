package br.ucsal.clinica.estoque.porta;

import java.util.Optional;

import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.estoque.dominio.SituacaoSolicitacao;
import br.ucsal.clinica.estoque.dominio.SolicitacaoMedicamento;

/** Porta de persistencia das solicitacoes de medicamento. */
public interface SolicitacaoMedicamentoRepositoryPort {

    Optional<SolicitacaoMedicamento> buscarPorId(Long id);

    SolicitacaoMedicamento salvar(SolicitacaoMedicamento solicitacao);

    Pagina<SolicitacaoMedicamento> listar(SituacaoSolicitacao situacao, Paginacao paginacao);

    Pagina<SolicitacaoMedicamento> listarDoSolicitante(Long colaboradorId, Paginacao paginacao);
}
