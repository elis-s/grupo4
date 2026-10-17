package br.ucsal.clinica.atendimento.aplicacao;

import br.ucsal.clinica.atendimento.dominio.AtendimentoClinico;
import br.ucsal.clinica.atendimento.porta.AtendimentoRepositoryPort;
import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;

/**
 * Caso de uso RegistrarEvolucaoClinica (RF017 a RF019): sintomas, diagnostico e tratamento
 * registrados no prontuario durante o atendimento, pelo colaborador responsavel.
 */
public class RegistrarEvolucaoClinica {

    public record Comando(String sintomas, String diagnostico, String tratamento, Long versao) {
    }

    private final AtendimentoRepositoryPort atendimentos;
    private final SecurityContextPort contexto;

    public RegistrarEvolucaoClinica(AtendimentoRepositoryPort atendimentos, SecurityContextPort contexto) {
        this.atendimentos = atendimentos;
        this.contexto = contexto;
    }

    public AtendimentoClinico executar(Long id, Comando comando) {
        AtendimentoClinico atendimento = atendimentos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atendimento", id));
        atendimento.verificarVersao(comando.versao());
        atendimento.garantirResponsavel(RealizarTriagemPaciente.medicoLogado(contexto).colaboradorId());
        atendimento.registrarEvolucao(comando.sintomas(), comando.diagnostico(), comando.tratamento());
        return atendimentos.salvar(atendimento);
    }
}
