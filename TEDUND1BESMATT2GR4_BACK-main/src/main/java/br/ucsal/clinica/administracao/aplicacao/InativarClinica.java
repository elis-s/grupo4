package br.ucsal.clinica.administracao.aplicacao;

import br.ucsal.clinica.administracao.dominio.Clinica;
import br.ucsal.clinica.administracao.porta.ClinicaRepositoryPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;

/** Caso de uso InativarClinica (RF003, RN021, RN023): nunca exclui, registra o motivo e preserva o historico. */
public class InativarClinica {

    private final ClinicaRepositoryPort repositorio;

    public InativarClinica(ClinicaRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Clinica executar(Long id, String motivo) {
        Clinica registro = repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Clínica", id));
        registro.inativar(motivo);
        return repositorio.salvar(registro);
    }
}
