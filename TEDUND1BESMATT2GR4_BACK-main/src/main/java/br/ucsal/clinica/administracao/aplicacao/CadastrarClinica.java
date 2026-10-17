package br.ucsal.clinica.administracao.aplicacao;

import br.ucsal.clinica.administracao.dominio.Clinica;
import br.ucsal.clinica.administracao.porta.ClinicaRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;

/** Caso de uso CadastrarClinica (RF001). */
public class CadastrarClinica {

    private final ClinicaRepositoryPort repositorio;

    public CadastrarClinica(ClinicaRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Clinica executar(Clinica nova) {
        nova.validar();
        if (repositorio.existeCnpj(nova.getCnpj(), null)) {
            throw new RegraNegocioException("Já existe uma clínica com este CNPJ.");
        }
        return repositorio.salvar(nova);
    }
}
