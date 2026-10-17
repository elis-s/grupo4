package br.ucsal.clinica.atendimento.aplicacao;

import br.ucsal.clinica.atendimento.dominio.AtendimentoClinico;
import br.ucsal.clinica.atendimento.porta.AtendimentoRepositoryPort;
import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;

/** Consulta dos atendimentos: todos (administrador) ou os do colaborador logado. */
public class ConsultarAtendimentos {

    private final AtendimentoRepositoryPort atendimentos;
    private final SecurityContextPort contexto;

    public ConsultarAtendimentos(AtendimentoRepositoryPort atendimentos, SecurityContextPort contexto) {
        this.atendimentos = atendimentos;
        this.contexto = contexto;
    }

    public AtendimentoClinico porId(Long id) {
        return atendimentos.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Atendimento", id));
    }

    public Pagina<AtendimentoClinico> todos(Paginacao paginacao) {
        return atendimentos.listar(paginacao);
    }

    public Pagina<AtendimentoClinico> meus(Paginacao paginacao) {
        Long colaboradorId = RealizarTriagemPaciente.colaboradorLogado(contexto).colaboradorId();
        return atendimentos.listarDoColaborador(colaboradorId, paginacao);
    }
}
