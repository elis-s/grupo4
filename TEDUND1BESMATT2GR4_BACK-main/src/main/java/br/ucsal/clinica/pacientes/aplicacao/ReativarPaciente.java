package br.ucsal.clinica.pacientes.aplicacao;

import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.pacientes.dominio.Paciente;
import br.ucsal.clinica.pacientes.porta.PacienteRepositoryPort;
import br.ucsal.clinica.pacientes.porta.ProntuarioRepositoryPort;

/** Reativa o paciente e o seu prontuario quando a restricao deixa de existir. */
public class ReativarPaciente {

    private final PacienteRepositoryPort pacientes;
    private final ProntuarioRepositoryPort prontuarios;

    public ReativarPaciente(PacienteRepositoryPort pacientes, ProntuarioRepositoryPort prontuarios) {
        this.pacientes = pacientes;
        this.prontuarios = prontuarios;
    }

    public Paciente executar(Long id) {
        Paciente paciente = pacientes.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", id));
        paciente.reativar();
        Paciente salvo = pacientes.salvar(paciente);
        prontuarios.buscarPorPaciente(id).filter(p -> !p.isAtivo()).ifPresent(p -> {
            p.reativar();
            prontuarios.salvar(p);
        });
        return salvo;
    }
}
