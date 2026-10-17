package br.ucsal.clinica.administracao.aplicacao;

import br.ucsal.clinica.administracao.dominio.Colaborador;
import br.ucsal.clinica.administracao.dominio.Especialidade;
import br.ucsal.clinica.administracao.dominio.Setor;
import br.ucsal.clinica.administracao.porta.ColaboradorRepositoryPort;
import br.ucsal.clinica.administracao.porta.EspecialidadeRepositoryPort;
import br.ucsal.clinica.administracao.porta.SetorRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;

/** Caso de uso CadastrarColaborador (RF010, RF047 medico, RF048 demais profissionais). */
public class CadastrarColaborador {

    private final ColaboradorRepositoryPort repositorio;
    private final SetorRepositoryPort setores;
    private final EspecialidadeRepositoryPort especialidades;

    public CadastrarColaborador(ColaboradorRepositoryPort repositorio, SetorRepositoryPort setores,
            EspecialidadeRepositoryPort especialidades) {
        this.repositorio = repositorio;
        this.setores = setores;
        this.especialidades = especialidades;
    }

    public Colaborador executar(Colaborador novo) {
        preparar(novo, null, repositorio, setores, especialidades);
        return repositorio.salvar(novo);
    }

    /** Valida o colaborador (RN024) e as referencias para setor e especialidade. */
    static void preparar(Colaborador colaborador, Long ignorarId, ColaboradorRepositoryPort repositorio,
            SetorRepositoryPort setores, EspecialidadeRepositoryPort especialidades) {
        colaborador.validar();
        Long setorId = colaborador.getSetor().id();
        Setor setor = setores.buscarPorId(setorId).orElseThrow(() -> new RecursoNaoEncontradoException("Setor", setorId));
        if (!setor.isAtivo()) {
            throw new RegraNegocioException("O setor (local de trabalho) informado está inativo.");
        }
        colaborador.setSetor(new Referencia(setor.getId(), setor.getNome()));

        if (colaborador.getEspecialidade() != null && colaborador.getEspecialidade().id() != null) {
            Long especialidadeId = colaborador.getEspecialidade().id();
            Especialidade especialidade = especialidades.buscarPorId(especialidadeId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Especialidade", especialidadeId));
            if (!especialidade.isAtiva()) {
                throw new RegraNegocioException("A especialidade informada está inativa.");
            }
            colaborador.setEspecialidade(new Referencia(especialidade.id(), especialidade.nome()));
        } else {
            colaborador.setEspecialidade(null);
        }

        if (repositorio.existeCpf(colaborador.getCpf(), ignorarId)) {
            throw new RegraNegocioException("Já existe um colaborador com este CPF.");
        }
        if (colaborador.getConselhoNumero() != null && repositorio.existeRegistroConselho(
                colaborador.getConselhoSigla(), colaborador.getConselhoUf(), colaborador.getConselhoNumero(), ignorarId)) {
            throw new RegraNegocioException("Já existe um colaborador com este registro no conselho.");
        }
    }
}
