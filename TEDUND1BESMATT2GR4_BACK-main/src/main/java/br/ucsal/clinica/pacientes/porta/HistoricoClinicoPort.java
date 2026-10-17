package br.ucsal.clinica.pacientes.porta;

import java.util.List;

import br.ucsal.clinica.pacientes.dominio.EvolucaoClinica;

/**
 * Leitura do historico imutavel do paciente (secao 3.2.3): as evolucoes clinicas
 * registradas pelo Modulo de Agendamento e Atendimento Clinico. A interface e
 * definida aqui, no nucleo do modulo de Pacientes, e implementada na infraestrutura.
 */
public interface HistoricoClinicoPort {

    List<EvolucaoClinica> doProntuario(Long prontuarioId);
}
