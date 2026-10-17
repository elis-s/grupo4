package br.ucsal.clinica.estoque.adaptador.web;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.ucsal.clinica.compartilhado.dominio.Status;
import br.ucsal.clinica.estoque.dominio.Armazenamento;
import br.ucsal.clinica.estoque.dominio.EstoqueInicial;
import br.ucsal.clinica.estoque.dominio.Medicamento;
import br.ucsal.clinica.estoque.dominio.MedicamentoEstoque;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class MedicamentoDTO {

    private MedicamentoDTO() {
    }

    /**
     * RF033: nome, descricao completa, fabricante ou fornecedor, armazenamento, quantidade em
     * estoque, validade e data de aquisicao. Os campos de estoque inicial so sao usados no cadastro.
     */
    public record Request(
            @NotBlank @Size(max = 150) String nome,
            @NotBlank String descricao,
            @NotBlank @Size(max = 150) String fabricante,
            @NotNull(message = "Informe REFRIGERADO ou TEMPERATURA_AMBIENTE") Armazenamento armazenamento,
            @Size(max = 20) String unidade,
            @Min(0) Integer estoqueMinimo,
            @Min(0) Integer quantidadeEmEstoque,
            @Size(max = 40) String lote,
            LocalDate dataValidade,
            LocalDate dataAquisicao,
            Long versao) {

        public Medicamento paraDominio() {
            Medicamento m = new Medicamento();
            m.setNome(nome);
            m.setDescricao(descricao);
            m.setFabricante(fabricante);
            m.setArmazenamento(armazenamento);
            m.setUnidade(unidade);
            m.setEstoqueMinimo(estoqueMinimo);
            return m;
        }

        public EstoqueInicial estoqueInicial() {
            return quantidadeEmEstoque == null || quantidadeEmEstoque == 0 ? null
                    : new EstoqueInicial(quantidadeEmEstoque, lote, dataValidade, dataAquisicao);
        }
    }

    public record Response(
            Long id,
            String nome,
            String descricao,
            String fabricante,
            Armazenamento armazenamento,
            String unidade,
            Integer estoqueMinimo,
            long saldoAtual,
            boolean reposicaoNecessaria,
            Long versao,
            LocalDateTime dataCadastro,
            Status status,
            String motivoInativacao,
            LocalDateTime dataInativacao) {

        public static Response de(MedicamentoEstoque me) {
            Medicamento m = me.medicamento();
            return new Response(m.getId(), m.getNome(), m.getDescricao(), m.getFabricante(), m.getArmazenamento(),
                    m.getUnidade(), m.getEstoqueMinimo(), me.saldo(), me.reposicaoNecessaria(), m.getVersao(),
                    m.getDataCadastro(), m.getStatus(), m.getMotivoInativacao(), m.getDataInativacao());
        }
    }
}
