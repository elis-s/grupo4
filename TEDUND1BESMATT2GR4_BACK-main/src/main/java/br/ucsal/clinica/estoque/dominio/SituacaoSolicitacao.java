package br.ucsal.clinica.estoque.dominio;

/**
 * Status da solicitacao de medicamento (RF023, RF059, RN033):
 * PENDENTE -> APROVADA ou REPROVADA (decisao do usuario/atendente);
 * APROVADA -> ENTREGUE quando a retirada e registrada no estoque.
 */
public enum SituacaoSolicitacao {
    PENDENTE,
    APROVADA,
    REPROVADA,
    ENTREGUE
}
