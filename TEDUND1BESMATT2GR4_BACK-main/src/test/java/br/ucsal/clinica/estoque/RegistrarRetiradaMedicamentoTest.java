package br.ucsal.clinica.estoque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.ucsal.clinica.autenticacao.dominio.NomePerfil;
import br.ucsal.clinica.compartilhado.dominio.Referencia;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.estoque.aplicacao.AprovarReprovarSolicitacao;
import br.ucsal.clinica.estoque.aplicacao.RegistrarReposicao;
import br.ucsal.clinica.estoque.aplicacao.RegistrarRetiradaMedicamento;
import br.ucsal.clinica.estoque.dominio.Armazenamento;
import br.ucsal.clinica.estoque.dominio.Medicamento;
import br.ucsal.clinica.estoque.dominio.MovimentacaoEstoque;
import br.ucsal.clinica.estoque.dominio.SituacaoSolicitacao;
import br.ucsal.clinica.estoque.dominio.SolicitacaoMedicamento;
import br.ucsal.clinica.estoque.dominio.TipoMovimentacao;
import br.ucsal.clinica.testes.Fakes;

/** Fluxo da secao 4.4: solicitacao aprovada -> retirada, com RN013, RN014, RN015, RN016, RN018 e RN032. */
class RegistrarRetiradaMedicamentoTest {

    private Fakes.Medicamentos medicamentos;
    private Fakes.Movimentacoes movimentacoes;
    private Fakes.Solicitacoes solicitacoes;
    private Fakes.Contexto atendente;
    private RegistrarRetiradaMedicamento retirar;
    private AprovarReprovarSolicitacao analisar;
    private Medicamento dipirona;

    @BeforeEach
    void preparar() {
        medicamentos = new Fakes.Medicamentos();
        movimentacoes = new Fakes.Movimentacoes(medicamentos);
        solicitacoes = new Fakes.Solicitacoes();
        atendente = Fakes.Contexto.de(7L, "joao.atendente", 5L, "ATENDENTE", NomePerfil.USUARIO);
        retirar = new RegistrarRetiradaMedicamento(solicitacoes, medicamentos, movimentacoes, atendente);
        analisar = new AprovarReprovarSolicitacao(solicitacoes, atendente);

        dipirona = new Medicamento();
        dipirona.setNome("Dipirona 500 mg");
        dipirona.setDescricao("Analgésico");
        dipirona.setFabricante("EMS");
        dipirona.setArmazenamento(Armazenamento.TEMPERATURA_AMBIENTE);
        dipirona.validar();
        medicamentos.salvar(dipirona);
        new RegistrarReposicao(medicamentos, movimentacoes).executar(new RegistrarReposicao.Comando(dipirona.getId(),
                12, "L1", LocalDate.now().plusYears(1), LocalDate.now(), null));
    }

    private SolicitacaoMedicamento solicitar(int quantidade) {
        return solicitacoes.salvar(SolicitacaoMedicamento.nova(new Referencia(dipirona.getId(), dipirona.getNome()),
                new Referencia(3L, "Dra. Ana Souza"), quantidade, "Paciente com febre", null));
    }

    @Test
    void naoRetiraSemAprovacao() {
        SolicitacaoMedicamento s = solicitar(2);
        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> retirar.executar(new RegistrarRetiradaMedicamento.Comando(s.getId(), dipirona.getId(), 2)));
        assertTrue(erro.getMessage().contains("APROVADA"));
    }

    @Test
    void retiradaRegistraSolicitanteResponsavelESaldo() {
        SolicitacaoMedicamento s = solicitar(2);
        analisar.aprovar(s.getId());

        RegistrarReposicao.Resultado r = retirar.executar(
                new RegistrarRetiradaMedicamento.Comando(s.getId(), dipirona.getId(), 2));

        MovimentacaoEstoque saida = r.movimentacao();
        assertEquals(TipoMovimentacao.SAIDA, saida.getTipo());
        assertEquals("Dra. Ana Souza", saida.getSolicitante().nome());              // RN013
        assertEquals("joao.atendente", saida.getResponsavelEntrega().nome());       // RN018
        assertTrue(saida.getDataHora() != null);                                   // RN014
        assertEquals(10, r.saldoAtual());
        assertTrue(r.reposicaoNecessaria());                                       // RN015/RN016: 10 unidades
        assertEquals(SituacaoSolicitacao.ENTREGUE, solicitacoes.buscarPorId(s.getId()).orElseThrow().getSituacao());
        assertEquals(2, medicamentos.bloqueios);                                   // bloqueio otimista: reposicao + retirada
    }

    @Test
    void naoRetiraMaisQueOSaldo() {
        SolicitacaoMedicamento s = solicitar(15);
        analisar.aprovar(s.getId());
        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> retirar.executar(new RegistrarRetiradaMedicamento.Comando(s.getId(), dipirona.getId(), 15)));
        assertEquals("Quantidade solicitada (15) maior que o saldo disponível (12).", erro.getMessage()); // RN032
    }

    @Test
    void reprovacaoExigeMotivoEEncerraASolicitacao() {
        SolicitacaoMedicamento s = solicitar(2);
        assertThrows(RegraNegocioException.class, () -> analisar.reprovar(s.getId(), " "));
        SolicitacaoMedicamento reprovada = analisar.reprovar(s.getId(), "Sem indicação clínica");
        assertEquals(SituacaoSolicitacao.REPROVADA, reprovada.getSituacao());
        assertEquals("joao.atendente", reprovada.getAnalisadoPor().nome());
        assertThrows(RegraNegocioException.class, () -> analisar.aprovar(s.getId()));    // RN033: ja analisada
    }

    @Test
    void reposicaoAcimaDoMinimoTiraOAlerta() {
        RegistrarReposicao.Resultado r = new RegistrarReposicao(medicamentos, movimentacoes).executar(
                new RegistrarReposicao.Comando(dipirona.getId(), 30, "L2", LocalDate.now().plusYears(1),
                        LocalDate.now(), "Compra mensal"));
        assertEquals(42, r.saldoAtual());
        assertFalse(r.reposicaoNecessaria());
    }
}
