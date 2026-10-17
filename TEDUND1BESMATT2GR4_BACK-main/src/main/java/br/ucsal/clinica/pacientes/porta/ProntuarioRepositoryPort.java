package br.ucsal.clinica.pacientes.porta;

import java.util.Optional;

import br.ucsal.clinica.pacientes.dominio.ProntuarioEletronico;

/** Porta de persistencia do prontuario eletronico (1:1 com o paciente). */
public interface ProntuarioRepositoryPort {

    Optional<ProntuarioEletronico> buscarPorId(Long id);

    Optional<ProntuarioEletronico> buscarPorPaciente(Long pacienteId);

    ProntuarioEletronico salvar(ProntuarioEletronico prontuario);
}
