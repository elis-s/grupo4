package br.ucsal.clinica.pacientes.aplicacao;

import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.pacientes.dominio.Paciente;
import br.ucsal.clinica.pacientes.porta.PacienteRepositoryPort;
import br.ucsal.clinica.pacientes.porta.ProntuarioRepositoryPort;

/**
 * Caso de uso InativarPaciente (RF050, RN011): havendo restricao para o paciente nao ser
 * mais atendido, o cadastro e o prontuario sao inativados, mantendo o historico.
 */
public class InativarPaciente {

    private final PacienteRepositoryPort pacientes;
    private final ProntuarioRepositoryPort prontuarios;

    public InativarPaciente(PacienteRepositoryPort pacientes, ProntuarioRepositoryPort prontuarios) {
        this.pacientes = pacientes;
        this.prontuarios = prontuarios;
    }

    public Paciente executar(Long id, String motivo) {
        Paciente paciente = pacientes.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", id));
        paciente.inativar(motivo);
        Paciente salvo = pacientes.salvar(paciente);
        prontuarios.buscarPorPaciente(id).filter(p -> p.isAtivo()).ifPresent(p -> {
            p.inativar(motivo);
            prontuarios.salvar(p);
        });
        return salvo;
    }
}
