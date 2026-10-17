package br.ucsal.clinica.administracao.porta;

import java.util.Optional;

import br.ucsal.clinica.administracao.dominio.Clinica;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.Status;

/** Porta de persistencia da clinica (implementada pelo adaptador JPA). */
public interface ClinicaRepositoryPort {

    Optional<Clinica> buscarPorId(Long id);

    /** Inclui ou altera; devolve a entidade com id e versao atualizados. */
    Clinica salvar(Clinica entidade);

    /** Listagem paginada, com filtro opcional de status. */
    Pagina<Clinica> listar(Status status, Paginacao paginacao);

    boolean existeCnpj(String cnpj, Long ignorarId);
}
