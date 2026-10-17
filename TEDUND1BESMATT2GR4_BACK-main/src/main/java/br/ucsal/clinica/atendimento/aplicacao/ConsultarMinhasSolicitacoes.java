package br.ucsal.clinica.atendimento.aplicacao;

import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.estoque.dominio.SolicitacaoMedicamento;
import br.ucsal.clinica.estoque.porta.SolicitacaoMedicamentoRepositoryPort;

/** Solicitacoes de medicamento feitas pelo colaborador logado, com a situacao de cada uma. */
public class ConsultarMinhasSolicitacoes {

    private final SolicitacaoMedicamentoRepositoryPort solicitacoes;
    private final SecurityContextPort contexto;

    public ConsultarMinhasSolicitacoes(SolicitacaoMedicamentoRepositoryPort solicitacoes, SecurityContextPort contexto) {
        this.solicitacoes = solicitacoes;
        this.contexto = contexto;
    }

    public Pagina<SolicitacaoMedicamento> executar(Paginacao paginacao) {
        Long colaboradorId = RealizarTriagemPaciente.colaboradorLogado(contexto).colaboradorId();
        return solicitacoes.listarDoSolicitante(colaboradorId, paginacao);
    }
}
