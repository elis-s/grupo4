package br.ucsal.clinica.pacientes.aplicacao;

import br.ucsal.clinica.administracao.dominio.Colaborador;
import br.ucsal.clinica.administracao.dominio.Convenio;
import br.ucsal.clinica.administracao.dominio.Especialidade;
import br.ucsal.clinica.administracao.porta.ColaboradorRepositoryPort;
import br.ucsal.clinica.administracao.porta.ConvenioRepositoryPort;
import br.ucsal.clinica.administracao.porta.EspecialidadeRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.pacientes.dominio.Paciente;
import br.ucsal.clinica.pacientes.dominio.ProntuarioEletronico;
import br.ucsal.clinica.pacientes.porta.PacienteRepositoryPort;
import br.ucsal.clinica.pacientes.porta.ProntuarioRepositoryPort;

/**
 * Caso de uso CadastrarPaciente (RF027, RF031, RF052) com InicializarProntuario:
 * o paciente e o seu prontuario sao criados juntos, na mesma transacao - nem paciente
 * sem prontuario, nem prontuario sem paciente (RN007 a RN009, secao 3.4.2, DA012).
 */
public class CadastrarPaciente {

    private final PacienteRepositoryPort pacientes;
    private final ProntuarioRepositoryPort prontuarios;
    private final ColaboradorRepositoryPort colaboradores;
    private final EspecialidadeRepositoryPort especialidades;
    private final ConvenioRepositoryPort convenios;

    public CadastrarPaciente(PacienteRepositoryPort pacientes, ProntuarioRepositoryPort prontuarios,
            ColaboradorRepositoryPort colaboradores, EspecialidadeRepositoryPort especialidades,
            ConvenioRepositoryPort convenios) {
        this.pacientes = pacientes;
        this.prontuarios = prontuarios;
        this.colaboradores = colaboradores;
        this.especialidades = especialidades;
        this.convenios = convenios;
    }

    public Paciente executar(Paciente novo) {
        preparar(novo, null, pacientes, colaboradores, especialidades, convenios);
        Paciente salvo = pacientes.salvar(novo);
        ProntuarioEletronico prontuario = prontuarios.salvar(ProntuarioEletronico.paraPaciente(salvo.getId()));
        salvo.setProntuario(new Referencia(prontuario.getId(), prontuario.getNumero()));
        return salvo;
    }

    /** Valida o paciente e confirma medico (ativo, funcao MEDICO), especialidade e convenio (ativo). */
    static void preparar(Paciente paciente, Long ignorarId, PacienteRepositoryPort pacientes,
            ColaboradorRepositoryPort colaboradores, EspecialidadeRepositoryPort especialidades,
            ConvenioRepositoryPort convenios) {
        paciente.validar();
        if (paciente.getCpf() != null && pacientes.existeCpf(paciente.getCpf(), ignorarId)) {
            throw new RegraNegocioException("Já existe um paciente com este CPF.");
        }
        Long medicoId = paciente.getMedico().id();
        Colaborador medico = colaboradores.buscarPorId(medicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador", medicoId));
        if (!medico.isMedico() || !medico.isAtivo()) {
            throw new RegraNegocioException(medico.getNomeCompleto() + " não é um médico ativo.");
        }
        paciente.setMedico(new Referencia(medico.getId(), medico.getNomeCompleto()));

        Long especialidadeId = paciente.getEspecialidade().id();
        Especialidade especialidade = especialidades.buscarPorId(especialidadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Especialidade", especialidadeId));
        paciente.setEspecialidade(new Referencia(especialidade.id(), especialidade.nome()));

        if (paciente.getConvenio() != null) {
            Long convenioId = paciente.getConvenio().id();
            Convenio convenio = convenios.buscarPorId(convenioId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Convênio", convenioId));
            if (!convenio.isAtivo()) {
                throw new RegraNegocioException("O convênio " + convenio.getNomeFantasia() + " está inativo.");
            }
            paciente.setConvenio(new Referencia(convenio.getId(), convenio.getNomeFantasia()));
        }
    }
}
