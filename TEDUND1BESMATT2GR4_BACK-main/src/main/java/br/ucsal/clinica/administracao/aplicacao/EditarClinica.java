package br.ucsal.clinica.administracao.aplicacao;

import br.ucsal.clinica.administracao.dominio.Clinica;
import br.ucsal.clinica.administracao.porta.ClinicaRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;

/** Caso de uso EditarClinica (RF002), com bloqueio otimista pela versao lida. */
public class EditarClinica {

    private final ClinicaRepositoryPort repositorio;

    public EditarClinica(ClinicaRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Clinica executar(Long id, Clinica dados, Long versaoLida) {
        Clinica clinica = repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Clínica", id));
        clinica.verificarVersao(versaoLida);
        dados.validar();
        if (repositorio.existeCnpj(dados.getCnpj(), id)) {
            throw new RegraNegocioException("Já existe outra clínica com este CNPJ.");
        }
        clinica.atualizarCom(dados);
        return repositorio.salvar(clinica);
    }
}
