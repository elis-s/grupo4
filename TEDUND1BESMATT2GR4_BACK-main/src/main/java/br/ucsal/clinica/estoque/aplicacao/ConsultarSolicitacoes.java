package br.ucsal.clinica.estoque.aplicacao;

import br.ucsal.clinica.autenticacao.dominio.NomePerfil;
import br.ucsal.clinica.autenticacao.dominio.UsuarioAutenticado;
import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.compartilhado.dominio.AcessoNegadoException;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.estoque.dominio.SituacaoSolicitacao;
import br.ucsal.clinica.estoque.dominio.SolicitacaoMedicamento;
import br.ucsal.clinica.estoque.porta.SolicitacaoMedicamentoRepositoryPort;

/**
 * Consulta das solicitacoes de medicamento. Autorizacao por dado (secao 6.2, nivel 3):
 * quem e apenas colaborador so ve as proprias solicitacoes.
 */
public class ConsultarSolicitacoes {

    private final SolicitacaoMedicamentoRepositoryPort solicitacoes;
    private final SecurityContextPort contexto;

    public ConsultarSolicitacoes(SolicitacaoMedicamentoRepositoryPort solicitacoes, SecurityContextPort contexto) {
        this.solicitacoes = solicitacoes;
        this.contexto = contexto;
    }

    public SolicitacaoMedicamento porId(Long id) {
        SolicitacaoMedicamento s = solicitacoes.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação", id));
        UsuarioAutenticado usuario = contexto.usuarioAtual();
        boolean analisa = usuario.isAdministrador() || usuario.temPerfil(NomePerfil.USUARIO);
        boolean solicitante = usuario.colaboradorId() != null
                && usuario.colaboradorId().equals(s.getSolicitante().id());
        if (!analisa && !solicitante) {
            throw new AcessoNegadoException("Você só pode consultar as suas próprias solicitações.");
        }
        return s;
    }

    /** Fila de solicitacoes para o atendente analisar (filtro opcional por situacao). */
    public Pagina<SolicitacaoMedicamento> listar(SituacaoSolicitacao situacao, Paginacao paginacao) {
        return solicitacoes.listar(situacao, paginacao);
    }
}
