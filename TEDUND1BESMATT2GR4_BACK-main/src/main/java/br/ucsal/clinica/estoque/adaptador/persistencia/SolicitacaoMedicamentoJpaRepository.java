package br.ucsal.clinica.estoque.adaptador.persistencia;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.ucsal.clinica.estoque.dominio.SituacaoSolicitacao;

public interface SolicitacaoMedicamentoJpaRepository extends JpaRepository<SolicitacaoMedicamentoEntity, Long> {

    Page<SolicitacaoMedicamentoEntity> findBySituacao(SituacaoSolicitacao situacao, Pageable pageable);

    Page<SolicitacaoMedicamentoEntity> findBySolicitanteId(Long colaboradorId, Pageable pageable);

    List<SolicitacaoMedicamentoEntity> findBySolicitanteIdOrderByDataCadastroDesc(Long colaboradorId);

    List<SolicitacaoMedicamentoEntity> findBySituacaoInOrderByDataAnaliseDesc(Collection<SituacaoSolicitacao> situacoes);

    List<SolicitacaoMedicamentoEntity> findAllByOrderByDataCadastroDesc();
}
