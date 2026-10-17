package br.ucsal.clinica.pacientes.aplicacao;

import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Status;
import br.ucsal.clinica.pacientes.dominio.Paciente;
import br.ucsal.clinica.pacientes.porta.PacienteRepositoryPort;

/** Caso de uso de consulta dos dados cadastrais do paciente (RF029). */
public class ConsultarPacientes {

    private final PacienteRepositoryPort pacientes;

    public ConsultarPacientes(PacienteRepositoryPort pacientes) {
        this.pacientes = pacientes;
    }

    public Paciente porId(Long id) {
        return pacientes.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", id));
    }

    public Pagina<Paciente> listar(Status status, String nome, Paginacao paginacao) {
        return pacientes.listar(status, nome, paginacao);
    }
}
