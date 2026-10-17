package br.ucsal.clinica.administracao.aplicacao;

import br.ucsal.clinica.administracao.dominio.Convenio;
import br.ucsal.clinica.administracao.porta.ConvenioRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;

/** Caso de uso EditarConvenio (RF008). */
public class EditarConvenio {

    private final ConvenioRepositoryPort repositorio;

    public EditarConvenio(ConvenioRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Convenio executar(Long id, Convenio dados, Long versaoLida) {
        Convenio convenio = repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Convênio", id));
        convenio.verificarVersao(versaoLida);
        CadastrarConvenio.validarUnicidade(repositorio, dados, id);
        convenio.atualizarCom(dados);
        return repositorio.salvar(convenio);
    }
}
