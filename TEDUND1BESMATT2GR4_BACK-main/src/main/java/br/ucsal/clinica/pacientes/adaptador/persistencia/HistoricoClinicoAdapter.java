package br.ucsal.clinica.pacientes.adaptador.persistencia;

import java.util.List;

import org.springframework.stereotype.Component;

import br.ucsal.clinica.atendimento.adaptador.persistencia.AtendimentoEntity;
import br.ucsal.clinica.atendimento.adaptador.persistencia.AtendimentoJpaRepository;
import br.ucsal.clinica.atendimento.adaptador.persistencia.OrientacaoEnfermagemJpaRepository;
import br.ucsal.clinica.pacientes.dominio.EvolucaoClinica;
import br.ucsal.clinica.pacientes.porta.HistoricoClinicoPort;

/**
 * Implementa, na infraestrutura, a porta de leitura do historico do PEP: le os atendimentos
 * do prontuario (com prescricoes e orientacoes) e os converte em evolucoes clinicas.
 */
@Component
public class HistoricoClinicoAdapter implements HistoricoClinicoPort {

    private final AtendimentoJpaRepository atendimentos;
    private final OrientacaoEnfermagemJpaRepository orientacoes;

    public HistoricoClinicoAdapter(AtendimentoJpaRepository atendimentos, OrientacaoEnfermagemJpaRepository orientacoes) {
        this.atendimentos = atendimentos;
        this.orientacoes = orientacoes;
    }

    @Override
    public List<EvolucaoClinica> doProntuario(Long prontuarioId) {
        return atendimentos.findByProntuarioIdOrderByDataHoraInicioDesc(prontuarioId).stream()
                .map(this::paraEvolucao).toList();
    }

    private EvolucaoClinica paraEvolucao(AtendimentoEntity a) {
        List<String> medicamentos = a.getPrescricoes().stream()
                .map(p -> p.getMedicamento().getNome() + " - " + p.getDosagem()
                        + (p.getPosologia() != null ? " (" + p.getPosologia() + ")" : ""))
                .toList();
        List<String> orientacoesEnfermagem = orientacoes.findByAtendimentoIdOrderByDataHora(a.getId()).stream()
                .map(o -> o.getDataHora() + " - " + o.getColaborador().getNomeCompleto() + ": " + o.getOrientacao())
                .toList();
        return new EvolucaoClinica(a.getId(), a.getDataHoraInicio(), a.getDataHoraEncerramento(), a.getTipo().name(),
                a.getEtapa().name(), a.getMedico().getNomeCompleto(),
                a.getConvenio() != null ? a.getConvenio().getNomeFantasia() : "Particular",
                a.getSintomas(), a.getDiagnostico(), a.getTratamento(), a.isInternado(), medicamentos,
                orientacoesEnfermagem, a.getStatus().name());
    }
}
