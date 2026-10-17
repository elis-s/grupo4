package br.ucsal.clinica.pacientes.aplicacao;

import br.ucsal.clinica.administracao.porta.ColaboradorRepositoryPort;
import br.ucsal.clinica.administracao.porta.ConvenioRepositoryPort;
import br.ucsal.clinica.administracao.porta.EspecialidadeRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.pacientes.dominio.Paciente;
import br.ucsal.clinica.pacientes.porta.PacienteRepositoryPort;

/** Caso de uso EditarPaciente (RF049) - inclui trocar o convenio, que faz parte do prontuario (RF052, RN026). */
public class EditarPaciente {

    private final PacienteRepositoryPort pacientes;
    private final ColaboradorRepositoryPort colaboradores;
    private final EspecialidadeRepositoryPort especialidades;
    private final ConvenioRepositoryPort convenios;

    public EditarPaciente(PacienteRepositoryPort pacientes, ColaboradorRepositoryPort colaboradores,
            EspecialidadeRepositoryPort especialidades, ConvenioRepositoryPort convenios) {
        this.pacientes = pacientes;
        this.colaboradores = colaboradores;
        this.especialidades = especialidades;
        this.convenios = convenios;
    }

    public Paciente executar(Long id, Paciente dados, Long versaoLida) {
        Paciente paciente = pacientes.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", id));
        paciente.verificarVersao(versaoLida);
        CadastrarPaciente.preparar(dados, id, pacientes, colaboradores, especialidades, convenios);
        paciente.atualizarCom(dados);
        return pacientes.salvar(paciente);
    }
}
