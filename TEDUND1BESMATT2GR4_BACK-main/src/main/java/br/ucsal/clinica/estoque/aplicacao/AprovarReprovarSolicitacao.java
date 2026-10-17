package br.ucsal.clinica.estoque.aplicacao;

import br.ucsal.clinica.autenticacao.dominio.UsuarioAutenticado;
import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.estoque.dominio.SolicitacaoMedicamento;
import br.ucsal.clinica.estoque.porta.SolicitacaoMedicamentoRepositoryPort;

/**
 * Caso de uso AprovarReprovarSolicitacao (RF059, RN033): o usuario (atendente) decide
 * sobre a solicitacao do colaborador antes da retirada; quem decidiu vem do login.
 */
public class AprovarReprovarSolicitacao {

    private final SolicitacaoMedicamentoRepositoryPort solicitacoes;
    private final SecurityContextPort contexto;

    public AprovarReprovarSolicitacao(SolicitacaoMedicamentoRepositoryPort solicitacoes, SecurityContextPort contexto) {
        this.solicitacoes = solicitacoes;
        this.contexto = contexto;
    }

    public SolicitacaoMedicamento aprovar(Long id) {
        SolicitacaoMedicamento solicitacao = obter(id);
        solicitacao.aprovar(usuarioLogado());
        return solicitacoes.salvar(solicitacao);
    }

    public SolicitacaoMedicamento reprovar(Long id, String motivo) {
        SolicitacaoMedicamento solicitacao = obter(id);
        solicitacao.reprovar(usuarioLogado(), motivo);
        return solicitacoes.salvar(solicitacao);
    }

    private SolicitacaoMedicamento obter(Long id) {
        return solicitacoes.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação", id));
    }

    private Referencia usuarioLogado() {
        UsuarioAutenticado usuario = contexto.usuarioAtual();
        return new Referencia(usuario.id(), usuario.login());
    }
}
