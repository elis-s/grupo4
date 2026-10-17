package br.ucsal.clinica.administracao.dominio;

/** Dia e turno em que o medico atende (RF047, RN024). */
public record HorarioAtendimento(DiaSemana diaSemana, Turno turno) {
}
