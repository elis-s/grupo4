package br.ucsal.clinica.atendimento.dominio;

/**
 * Etapas internas do atendimento clinico (secao 3.2.4, DA015): EM_TRIAGEM -> EM_ATENDIMENTO
 * -> FINALIZADO. Nao substituem os status do agendamento definidos no cenario.
 */
public enum EtapaAtendimento {
    EM_TRIAGEM,
    EM_ATENDIMENTO,
    FINALIZADO
}
