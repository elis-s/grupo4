package br.ucsal.clinica.atendimento.aplicacao;

import java.time.LocalDate;
import java.util.List;

import br.ucsal.clinica.atendimento.dominio.Agendamento;
import br.ucsal.clinica.atendimento.porta.AgendamentoRepositoryPort;
import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.compartilhado.dominio.Pagina;
import br.ucsal.clinica.compartilhado.dominio.Paginacao;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;

/** Caso de uso de consulta dos agendamentos (RF026) e da agenda do colaborador logado. */
public class ConsultarAgendamentos {

    private final AgendamentoRepositoryPort agendamentos;
    private final SecurityContextPort contexto;

    public ConsultarAgendamentos(AgendamentoRepositoryPort agendamentos, SecurityContextPort contexto) {
        this.agendamentos = agendamentos;
        this.contexto = contexto;
    }

    public Agendamento porId(Long id) {
        return agendamentos.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento", id));
    }

    public Pagina<Agendamento> listar(LocalDate data, Long medicoId, Long pacienteId, Paginacao paginacao) {
        return agendamentos.listar(data, medicoId, pacienteId, paginacao);
    }

    /** Agenda do colaborador logado, de hoje em diante. */
    public List<Agendamento> minhaAgenda() {
        Long colaboradorId = RealizarTriagemPaciente.colaboradorLogado(contexto).colaboradorId();
        return agendamentos.agendaDoMedico(colaboradorId, LocalDate.now());
    }
}
