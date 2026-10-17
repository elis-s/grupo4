package br.ucsal.clinica.administracao.porta;

import java.util.Optional;

import br.ucsal.clinica.administracao.dominio.Setor;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.Status;

/** Porta de persistencia dos setores. */
public interface SetorRepositoryPort {

    Optional<Setor> buscarPorId(Long id);

    /** Inclui ou altera; devolve a entidade com id e versao atualizados. */
    Setor salvar(Setor entidade);

    /** Listagem paginada, com filtro opcional de status. */
    Pagina<Setor> listar(Status status, Paginacao paginacao);

    boolean existeNomeNaClinica(Long clinicaId, String nome, Long ignorarId);
}
