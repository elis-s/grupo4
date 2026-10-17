package br.ucsal.clinica.atendimento.aplicacao;

import br.ucsal.clinica.administracao.porta.ColaboradorRepositoryPort;
import br.ucsal.clinica.atendimento.dominio.Agendamento;
import br.ucsal.clinica.atendimento.porta.AgendamentoRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.pacientes.porta.PacienteRepositoryPort;

/** Caso de uso EditarAgendamento (RF053). */
public class EditarAgendamento {

    private final AgendamentoRepositoryPort agendamentos;
    private final ColaboradorRepositoryPort colaboradores;
    private final PacienteRepositoryPort pacientes;

    public EditarAgendamento(AgendamentoRepositoryPort agendamentos, ColaboradorRepositoryPort colaboradores,
            PacienteRepositoryPort pacientes) {
        this.agendamentos = agendamentos;
        this.colaboradores = colaboradores;
        this.pacientes = pacientes;
    }

    public Agendamento executar(Long id, Agendamento dados, Long versaoLida) {
        Agendamento agendamento = agendamentos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento", id));
        agendamento.verificarVersao(versaoLida);
        agendamento.garantirEditavel();
        AgendarConsulta.preparar(dados, colaboradores, pacientes);
        AgendarConsulta.garantirHorarioLivre(agendamentos, dados, id);
        agendamento.atualizarCom(dados);
        return agendamentos.salvar(agendamento);
    }
}
