package br.ucsal.clinica.relatorios.aplicacao;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import br.ucsal.clinica.autenticacao.porta.SecurityContextPort;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.relatorios.dominio.LinhasRelatorio.LinhaMedicamento;
import br.ucsal.clinica.relatorios.dominio.Relatorios.BoletimMovimentacaoFarmacia;
import br.ucsal.clinica.relatorios.porta.RelatorioConsultaPort;

/** Caso de uso EmitirBoletimMovimentacaoFarmacia (RF043): movimentacoes de estoque e saldos no periodo. */
public class EmitirBoletimMovimentacaoFarmacia {

    private final RelatorioConsultaPort consultas;
    private final SecurityContextPort contexto;

    public EmitirBoletimMovimentacaoFarmacia(RelatorioConsultaPort consultas, SecurityContextPort contexto) {
        this.consultas = consultas;
        this.contexto = contexto;
    }

    public BoletimMovimentacaoFarmacia executar(LocalDate inicio, LocalDate fim) {
        LocalDate de = inicio != null ? inicio : LocalDate.now().withDayOfMonth(1);
        LocalDate ate = fim != null ? fim : LocalDate.now();
        if (ate.isBefore(de)) {
            throw new RegraNegocioException("A data final não pode ser anterior à data inicial.");
        }
        var movimentacoes = consultas.movimentacoes(de.atStartOfDay(), ate.plusDays(1).atStartOfDay(), null);
        var saldos = consultas.medicamentos();

        Map<String, Long> totais = new LinkedHashMap<>();
        totais.put("movimentacoes", (long) movimentacoes.size());
        totais.put("unidadesQueEntraram", movimentacoes.stream()
                .filter(m -> "ENTRADA".equals(m.tipo())).mapToLong(m -> m.quantidade()).sum());
        totais.put("unidadesQueSairam", movimentacoes.stream()
                .filter(m -> "SAIDA".equals(m.tipo())).mapToLong(m -> m.quantidade()).sum());
        totais.put("medicamentosParaRepor", saldos.stream().filter(LinhaMedicamento::reposicaoNecessaria).count());

        return new BoletimMovimentacaoFarmacia(EmitirRelatorioAdministrativoCompleto.emissor(contexto.usuarioAtual()),
                de, ate, totais, saldos, movimentacoes);
    }
}
