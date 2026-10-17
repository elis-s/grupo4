package br.ucsal.clinica.pacientes.aplicacao;

import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.pacientes.dominio.ProntuarioEletronico;
import br.ucsal.clinica.pacientes.porta.ProntuarioRepositoryPort;

/** Caso de uso InativarProntuario (RF051, RN029): pode ser inativado, nunca excluido. */
public class InativarProntuario {

    private final ProntuarioRepositoryPort prontuarios;

    public InativarProntuario(ProntuarioRepositoryPort prontuarios) {
        this.prontuarios = prontuarios;
    }

    public ProntuarioEletronico executar(Long id, String motivo) {
        ProntuarioEletronico prontuario = prontuarios.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prontuário", id));
        prontuario.inativar(motivo);
        return prontuarios.salvar(prontuario);
    }
}
