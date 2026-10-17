package br.ucsal.clinica.administracao.aplicacao;

import br.ucsal.clinica.administracao.dominio.Colaborador;
import br.ucsal.clinica.administracao.porta.ColaboradorRepositoryPort;
import br.ucsal.clinica.administracao.porta.EspecialidadeRepositoryPort;
import br.ucsal.clinica.administracao.porta.SetorRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;

/** Caso de uso EditarColaborador (RF011). */
public class EditarColaborador {

    private final ColaboradorRepositoryPort repositorio;
    private final SetorRepositoryPort setores;
    private final EspecialidadeRepositoryPort especialidades;

    public EditarColaborador(ColaboradorRepositoryPort repositorio, SetorRepositoryPort setores,
            EspecialidadeRepositoryPort especialidades) {
        this.repositorio = repositorio;
        this.setores = setores;
        this.especialidades = especialidades;
    }

    public Colaborador executar(Long id, Colaborador dados, Long versaoLida) {
        Colaborador colaborador = repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador", id));
        colaborador.verificarVersao(versaoLida);
        CadastrarColaborador.preparar(dados, id, repositorio, setores, especialidades);
        colaborador.atualizarCom(dados);
        return repositorio.salvar(colaborador);
    }
}
