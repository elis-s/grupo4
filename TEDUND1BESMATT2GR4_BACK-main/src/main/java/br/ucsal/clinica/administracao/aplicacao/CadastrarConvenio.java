package br.ucsal.clinica.administracao.aplicacao;

import br.ucsal.clinica.administracao.dominio.Convenio;
import br.ucsal.clinica.administracao.porta.ConvenioRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;

/** Caso de uso CadastrarConvenio (RF007). */
public class CadastrarConvenio {

    private final ConvenioRepositoryPort repositorio;

    public CadastrarConvenio(ConvenioRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Convenio executar(Convenio novo) {
        validarUnicidade(repositorio, novo, null);
        return repositorio.salvar(novo);
    }

    static void validarUnicidade(ConvenioRepositoryPort repositorio, Convenio convenio, Long ignorarId) {
        if (repositorio.existeCnpj(convenio.getCnpj(), ignorarId)) {
            throw new RegraNegocioException("Já existe um convênio com este CNPJ.");
        }
        if (repositorio.existeRegistroAns(convenio.getRegistroAns(), ignorarId)) {
            throw new RegraNegocioException("Já existe um convênio com este registro ANS.");
        }
    }
}
