package br.ucsal.clinica.pacientes.aplicacao;

import java.time.LocalDateTime;

import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.pacientes.dominio.AgendamentoParaRecepcao;
import br.ucsal.clinica.pacientes.dominio.Paciente;
import br.ucsal.clinica.pacientes.dominio.ProntuarioEletronico;
import br.ucsal.clinica.pacientes.dominio.RecepcaoPaciente;
import br.ucsal.clinica.pacientes.porta.AgendamentoRecepcaoPort;
import br.ucsal.clinica.pacientes.porta.PacienteRepositoryPort;
import br.ucsal.clinica.pacientes.porta.ProntuarioRepositoryPort;

/**
 * Caso de uso RegistrarRecepcaoPaciente (RF028): o atendente localiza o agendamento,
 * confirma a chegada do paciente e obtem o prontuario dele - que ja existe, pois e
 * gerado no cadastro do paciente (DA012).
 */
public class RegistrarRecepcaoPaciente {

    private final AgendamentoRecepcaoPort agendamentos;
    private final PacienteRepositoryPort pacientes;
    private final ProntuarioRepositoryPort prontuarios;

    public RegistrarRecepcaoPaciente(AgendamentoRecepcaoPort agendamentos, PacienteRepositoryPort pacientes,
            ProntuarioRepositoryPort prontuarios) {
        this.agendamentos = agendamentos;
        this.pacientes = pacientes;
        this.prontuarios = prontuarios;
    }

    public RecepcaoPaciente executar(Long agendamentoId) {
        AgendamentoParaRecepcao agendamento = agendamentos.buscar(agendamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento", agendamentoId));
        if (!agendamento.ativo()) {
            throw new RegraNegocioException("Agendamento inativo: " + agendamento.motivoInativacao());
        }
        if (agendamento.finalizado()) {
            throw new RegraNegocioException("Este agendamento já foi finalizado.");
        }
        if (agendamento.dataRecepcao() != null) {
            throw new RegraNegocioException("A chegada deste paciente já foi registrada.");
        }
        Paciente paciente = pacientes.buscarPorId(agendamento.pacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", agendamento.pacienteId()));
        paciente.garantirAtivoParaAtendimento();
        ProntuarioEletronico prontuario = prontuarios.buscarPorPaciente(paciente.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prontuário do paciente não encontrado."));
        prontuario.garantirAtivoParaAtendimento();

        LocalDateTime agora = LocalDateTime.now();
        agendamentos.registrarChegada(agendamentoId, agora);
        return new RecepcaoPaciente(agendamento, agora, paciente, prontuario);
    }
}
