package br.ucsal.clinica.atendimento.aplicacao;

import br.ucsal.clinica.atendimento.dominio.AtendimentoClinico;
import br.ucsal.clinica.atendimento.porta.AtendimentoRepositoryPort;
import br.ucsal.clinica.autenticacao.dominio.UsuarioAutenticado;
import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;

/** Inativa um atendimento registrado por engano (somente o responsavel ou o administrador). */
public class InativarAtendimento {

    private final AtendimentoRepositoryPort atendimentos;
    private final SecurityContextPort contexto;

    public InativarAtendimento(AtendimentoRepositoryPort atendimentos, SecurityContextPort contexto) {
        this.atendimentos = atendimentos;
        this.contexto = contexto;
    }

    public AtendimentoClinico executar(Long id, String motivo) {
        AtendimentoClinico atendimento = atendimentos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atendimento", id));
        UsuarioAutenticado usuario = contexto.usuarioAtual();
        if (!usuario.isAdministrador()) {
            atendimento.garantirResponsavel(usuario.colaboradorId());
        }
        atendimento.inativar(motivo);
        return atendimentos.salvar(atendimento);
    }
}
