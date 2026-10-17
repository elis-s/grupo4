package br.ucsal.clinica.atendimento.dominio;

/**
 * Status do agendamento conforme o cenario (RN010, DA015): inicia AGENDADO e muda para
 * FINALIZADO apos o atendimento. O status INATIVO (RN030) vem da inativacao logica.
 */
public enum SituacaoAgendamento {
    AGENDADO,
    FINALIZADO
}
