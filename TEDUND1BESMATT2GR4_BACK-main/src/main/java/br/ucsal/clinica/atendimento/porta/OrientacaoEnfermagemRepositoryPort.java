package br.ucsal.clinica.atendimento.porta;

import java.util.List;

import br.ucsal.clinica.atendimento.dominio.OrientacaoEnfermagem;

/** Porta de persistencia das orientacoes de enfermagem (somente inclusao). */
public interface OrientacaoEnfermagemRepositoryPort {

    OrientacaoEnfermagem registrar(OrientacaoEnfermagem orientacao);

    List<OrientacaoEnfermagem> listarDoAtendimento(Long atendimentoId);
}
