package br.ucsal.clinica.atendimento.aplicacao;

import br.ucsal.clinica.atendimento.dominio.AtendimentoClinico;
import br.ucsal.clinica.atendimento.dominio.OrientacaoEnfermagem;
import br.ucsal.clinica.atendimento.porta.AtendimentoRepositoryPort;
import br.ucsal.clinica.atendimento.porta.OrientacaoEnfermagemRepositoryPort;
import br.ucsal.clinica.autenticacao.dominio.UsuarioAutenticado;
import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.compartilhado.dominio.RecursoNaoEncontradoException;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;

/**
 * Caso de uso RegistrarOrientacaoEnfermagem (RF022): quando o paciente esta internado ou
 * em atendimento que exige acompanhamento de enfermagem.
 */
public class RegistrarOrientacaoEnfermagem {

    private final AtendimentoRepositoryPort atendimentos;
    private final OrientacaoEnfermagemRepositoryPort orientacoes;
    private final SecurityContextPort contexto;

    public RegistrarOrientacaoEnfermagem(AtendimentoRepositoryPort atendimentos,
            OrientacaoEnfermagemRepositoryPort orientacoes, SecurityContextPort contexto) {
        this.atendimentos = atendimentos;
        this.orientacoes = orientacoes;
        this.contexto = contexto;
    }

    public OrientacaoEnfermagem executar(Long atendimentoId, String orientacao) {
        UsuarioAutenticado colaborador = RealizarTriagemPaciente.colaboradorLogado(contexto);
        AtendimentoClinico atendimento = atendimentos.buscarPorId(atendimentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atendimento", atendimentoId));
        if (!atendimento.permiteOrientacaoEnfermagem()) {
            throw new RegraNegocioException(
                    "Orientações de enfermagem só para atendimento em andamento ou paciente internado (RF022).");
        }
        return orientacoes.registrar(OrientacaoEnfermagem.nova(atendimentoId,
                new Referencia(colaborador.colaboradorId(), colaborador.nome()), orientacao));
    }
}
