package br.ucsal.clinica.atendimento.aplicacao;

import br.ucsal.clinica.atendimento.dominio.AtendimentoClinico;
import br.ucsal.clinica.atendimento.porta.AtendimentoRepositoryPort;
import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;

/** Caso de uso IniciarAtendimentoClinico: EM_TRIAGEM -> EM_ATENDIMENTO (secao 3.2.4). */
public class IniciarAtendimentoClinico {

    private final AtendimentoRepositoryPort atendimentos;
    private final SecurityContextPort contexto;

    public IniciarAtendimentoClinico(AtendimentoRepositoryPort atendimentos, SecurityContextPort contexto) {
        this.atendimentos = atendimentos;
        this.contexto = contexto;
    }

    public AtendimentoClinico executar(Long id) {
        AtendimentoClinico atendimento = atendimentos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atendimento", id));
        atendimento.garantirResponsavel(RealizarTriagemPaciente.medicoLogado(contexto).colaboradorId());
        atendimento.iniciarAtendimento();
        return atendimentos.salvar(atendimento);
    }
}
