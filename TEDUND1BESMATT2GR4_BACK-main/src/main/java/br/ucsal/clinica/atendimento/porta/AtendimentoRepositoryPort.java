package br.ucsal.clinica.atendimento.porta;

import java.util.Optional;

import br.ucsal.clinica.atendimento.dominio.AtendimentoClinico;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;

/** Porta de persistencia dos atendimentos clinicos. */
public interface AtendimentoRepositoryPort {

    Optional<AtendimentoClinico> buscarPorId(Long id);

    AtendimentoClinico salvar(AtendimentoClinico atendimento);

    Pagina<AtendimentoClinico> listar(Paginacao paginacao);

    Pagina<AtendimentoClinico> listarDoColaborador(Long colaboradorId, Paginacao paginacao);

    boolean existeParaAgendamento(Long agendamentoId);
}
