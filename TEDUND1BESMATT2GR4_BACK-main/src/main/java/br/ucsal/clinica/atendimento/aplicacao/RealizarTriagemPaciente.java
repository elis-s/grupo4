package br.ucsal.clinica.atendimento.aplicacao;

import br.ucsal.clinica.administracao.dominio.Funcao;
import br.ucsal.clinica.atendimento.dominio.Agendamento;
import br.ucsal.clinica.atendimento.dominio.AtendimentoClinico;
import br.ucsal.clinica.atendimento.dominio.TipoAtendimento;
import br.ucsal.clinica.atendimento.porta.AgendamentoRepositoryPort;
import br.ucsal.clinica.atendimento.porta.AtendimentoRepositoryPort;
import br.ucsal.clinica.autenticacao.dominio.UsuarioAutenticado;
import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.compartilhado.dominio.AcessoNegadoException;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.pacientes.dominio.Paciente;
import br.ucsal.clinica.pacientes.dominio.ProntuarioEletronico;
import br.ucsal.clinica.pacientes.porta.PacienteRepositoryPort;
import br.ucsal.clinica.pacientes.porta.ProntuarioRepositoryPort;

/**
 * Caso de uso RealizarTriagemPaciente (RF016, RF017, RF021): abre o atendimento no
 * prontuario do paciente, na etapa EM_TRIAGEM, com o tipo e os sintomas.
 * O colaborador responsavel e capturado do usuario autenticado, nunca informado (RN028).
 */
public class RealizarTriagemPaciente {

    public record Comando(Long pacienteId, Long agendamentoId, TipoAtendimento tipo, String sintomas) {
    }

    private final AtendimentoRepositoryPort atendimentos;
    private final AgendamentoRepositoryPort agendamentos;
    private final PacienteRepositoryPort pacientes;
    private final ProntuarioRepositoryPort prontuarios;
    private final SecurityContextPort contexto;

    public RealizarTriagemPaciente(AtendimentoRepositoryPort atendimentos, AgendamentoRepositoryPort agendamentos,
            PacienteRepositoryPort pacientes, ProntuarioRepositoryPort prontuarios, SecurityContextPort contexto) {
        this.atendimentos = atendimentos;
        this.agendamentos = agendamentos;
        this.pacientes = pacientes;
        this.prontuarios = prontuarios;
        this.contexto = contexto;
    }

    public AtendimentoClinico executar(Comando comando) {
        UsuarioAutenticado medico = medicoLogado(contexto); // RN028
        Paciente paciente = pacientes.buscarPorId(comando.pacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", comando.pacienteId()));
        paciente.garantirAtivoParaAtendimento();
        ProntuarioEletronico prontuario = prontuarios.buscarPorPaciente(paciente.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prontuário do paciente não encontrado."));
        prontuario.garantirAtivoParaAtendimento(); // RN029

        if (comando.agendamentoId() != null) {
            validarAgendamento(comando.agendamentoId(), paciente, medico);
        }
        AtendimentoClinico atendimento = AtendimentoClinico.abrirTriagem(
                new Referencia(prontuario.getId(), prontuario.getNumero()),
                new Referencia(paciente.getId(), paciente.getNomeCompleto()),
                new Referencia(medico.colaboradorId(), medico.nome()),
                paciente.getConvenio(), comando.agendamentoId(), comando.tipo(), comando.sintomas());
        return atendimentos.salvar(atendimento);
    }

    private void validarAgendamento(Long agendamentoId, Paciente paciente, UsuarioAutenticado medico) {
        Agendamento agendamento = agendamentos.buscarPorId(agendamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento", agendamentoId));
        if (!agendamento.isAtivo()) {
            throw new RegraNegocioException("O agendamento informado está inativo.");
        }
        if (agendamento.isFinalizado() || atendimentos.existeParaAgendamento(agendamentoId)) {
            throw new RegraNegocioException("Este agendamento já foi atendido.");
        }
        if (!agendamento.getPaciente().id().equals(paciente.getId())) {
            throw new RegraNegocioException("O agendamento informado é de outro paciente.");
        }
        if (!agendamento.getMedico().id().equals(medico.colaboradorId())) {
            throw new RegraNegocioException("O agendamento informado é de outro médico.");
        }
    }

    /** O usuario logado precisa estar vinculado a um colaborador (RN037). */
    static UsuarioAutenticado colaboradorLogado(SecurityContextPort contexto) {
        UsuarioAutenticado usuario = contexto.usuarioAtual();
        if (usuario.colaboradorId() == null) {
            throw new AcessoNegadoException("Seu usuário não está vinculado a um colaborador.");
        }
        return usuario;
    }

    /** O atendimento clinico (diagnostico, prescricao) e registrado pelo colaborador medico. */
    static UsuarioAutenticado medicoLogado(SecurityContextPort contexto) {
        UsuarioAutenticado usuario = colaboradorLogado(contexto);
        if (!Funcao.MEDICO.name().equals(usuario.funcao())) {
            throw new AcessoNegadoException("Somente o colaborador médico registra o atendimento clínico.");
        }
        return usuario;
    }
}
