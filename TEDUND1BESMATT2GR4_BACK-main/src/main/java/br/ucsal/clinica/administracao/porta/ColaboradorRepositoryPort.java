package br.ucsal.clinica.administracao.porta;

import java.util.List;
import java.util.Optional;

import br.ucsal.clinica.administracao.dominio.Colaborador;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.Status;

/** Porta de persistencia dos colaboradores. */
public interface ColaboradorRepositoryPort {

    Optional<Colaborador> buscarPorId(Long id);

    /** Inclui ou altera; devolve a entidade com id e versao atualizados. */
    Colaborador salvar(Colaborador entidade);

    /** Listagem paginada, com filtro opcional de status. */
    Pagina<Colaborador> listar(Status status, Paginacao paginacao);

    boolean existeCpf(String cpf, Long ignorarId);

    boolean existeRegistroConselho(String sigla, String uf, String numero, Long ignorarId);

    /** Medicos ativos, para o agendamento e o cadastro de pacientes. */
    List<Colaborador> listarMedicosAtivos();
}
