package br.ucsal.clinica.administracao.porta;

import java.util.Optional;

import br.ucsal.clinica.administracao.dominio.Convenio;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.Status;

/** Porta de persistencia dos convenios. */
public interface ConvenioRepositoryPort {

    Optional<Convenio> buscarPorId(Long id);

    /** Inclui ou altera; devolve a entidade com id e versao atualizados. */
    Convenio salvar(Convenio entidade);

    /** Listagem paginada, com filtro opcional de status. */
    Pagina<Convenio> listar(Status status, Paginacao paginacao);

    boolean existeCnpj(String cnpj, Long ignorarId);

    boolean existeRegistroAns(String registroAns, Long ignorarId);
}
