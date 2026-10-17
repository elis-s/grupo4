package br.ucsal.clinica.pacientes.porta;

import java.util.Optional;

import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.Status;
import br.ucsal.clinica.pacientes.dominio.Paciente;

/** Porta de persistencia dos pacientes. */
public interface PacienteRepositoryPort {

    Optional<Paciente> buscarPorId(Long id);

    Paciente salvar(Paciente paciente);

    /** RF029: consulta com filtro opcional por nome (parte do nome) e por status. */
    Pagina<Paciente> listar(Status status, String nome, Paginacao paginacao);

    boolean existeCpf(String cpf, Long ignorarId);
}
