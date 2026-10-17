package br.ucsal.clinica.pacientes.aplicacao;

import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.pacientes.dominio.ProntuarioEletronico;
import br.ucsal.clinica.pacientes.porta.PacienteRepositoryPort;
import br.ucsal.clinica.pacientes.porta.ProntuarioRepositoryPort;

/** Reativa o prontuario; o paciente precisa estar ativo. */
public class ReativarProntuario {

    private final ProntuarioRepositoryPort prontuarios;
    private final PacienteRepositoryPort pacientes;

    public ReativarProntuario(ProntuarioRepositoryPort prontuarios, PacienteRepositoryPort pacientes) {
        this.prontuarios = prontuarios;
        this.pacientes = pacientes;
    }

    public ProntuarioEletronico executar(Long id) {
        ProntuarioEletronico prontuario = prontuarios.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prontuário", id));
        boolean pacienteAtivo = pacientes.buscarPorId(prontuario.getPacienteId()).map(p -> p.isAtivo()).orElse(false);
        if (!pacienteAtivo) {
            throw new RegraNegocioException("Reative primeiro o cadastro do paciente.");
        }
        prontuario.reativar();
        return prontuarios.salvar(prontuario);
    }
}
