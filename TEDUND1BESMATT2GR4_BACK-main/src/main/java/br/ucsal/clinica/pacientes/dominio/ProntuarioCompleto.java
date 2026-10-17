package br.ucsal.clinica.pacientes.dominio;

import java.util.List;

/** PEP consultado (RF030, RN026): o prontuario, o paciente com o convenio e o historico de evolucoes. */
public record ProntuarioCompleto(ProntuarioEletronico prontuario, Paciente paciente, List<EvolucaoClinica> evolucoes) {
}
