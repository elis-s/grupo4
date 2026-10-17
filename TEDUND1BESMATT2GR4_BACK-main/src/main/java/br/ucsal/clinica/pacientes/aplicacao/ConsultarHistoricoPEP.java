package br.ucsal.clinica.pacientes.aplicacao;

import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.pacientes.dominio.Paciente;
import br.ucsal.clinica.pacientes.dominio.ProntuarioCompleto;
import br.ucsal.clinica.pacientes.dominio.ProntuarioEletronico;
import br.ucsal.clinica.pacientes.porta.HistoricoClinicoPort;
import br.ucsal.clinica.pacientes.porta.PacienteRepositoryPort;
import br.ucsal.clinica.pacientes.porta.ProntuarioRepositoryPort;

/**
 * Caso de uso ConsultarHistoricoPEP (RF030, RN026): dados do paciente com o convenio
 * e todo o historico de atendimentos. Toda consulta a prontuario e auditada (secao 6.6).
 */
public class ConsultarHistoricoPEP {

    private final ProntuarioRepositoryPort prontuarios;
    private final PacienteRepositoryPort pacientes;
    private final HistoricoClinicoPort historico;

    public ConsultarHistoricoPEP(ProntuarioRepositoryPort prontuarios, PacienteRepositoryPort pacientes,
            HistoricoClinicoPort historico) {
        this.prontuarios = prontuarios;
        this.pacientes = pacientes;
        this.historico = historico;
    }

    public ProntuarioCompleto porProntuario(Long prontuarioId) {
        ProntuarioEletronico prontuario = prontuarios.buscarPorId(prontuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prontuário", prontuarioId));
        return montar(prontuario);
    }

    public ProntuarioCompleto porPaciente(Long pacienteId) {
        ProntuarioEletronico prontuario = prontuarios.buscarPorPaciente(pacienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Prontuário do paciente " + pacienteId + " não encontrado."));
        return montar(prontuario);
    }

    private ProntuarioCompleto montar(ProntuarioEletronico prontuario) {
        Paciente paciente = pacientes.buscarPorId(prontuario.getPacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", prontuario.getPacienteId()));
        return new ProntuarioCompleto(prontuario, paciente, historico.doProntuario(prontuario.getId()));
    }
}
