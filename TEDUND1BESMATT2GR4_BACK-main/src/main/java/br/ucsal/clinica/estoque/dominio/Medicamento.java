package br.ucsal.clinica.estoque.dominio;

import br.ucsal.clinica.compartilhado.dominio.EntidadeDominio;
import br.ucsal.clinica.compartilhado.dominio.RegraNegocioException;
import br.ucsal.clinica.compartilhado.dominio.Textos;

/**
 * Medicamento (RF033, RF034, RF055, RF056). A quantidade em estoque nao e um campo:
 * o saldo e sempre calculado pelas movimentacoes (RF036, RF057). Pode ser inativado,
 * mas o historico de movimentacoes e mantido (RN012).
 */
public class Medicamento extends EntidadeDominio {

    /** RN015: o nivel minimo de estoque e de 10 unidades. */
    public static final int NIVEL_MINIMO_PADRAO = 10;

    private String nome;
    private String descricao;
    private String fabricante;
    private Armazenamento armazenamento;
    private String unidade;
    private Integer estoqueMinimo = NIVEL_MINIMO_PADRAO;

    public void validar() {
        nome = Textos.obrigatorio(nome, "Informe o nome do medicamento.");
        descricao = Textos.obrigatorio(descricao, "Informe a descrição completa do medicamento.");
        fabricante = Textos.obrigatorio(fabricante, "Informe o fabricante ou fornecedor.");
        if (armazenamento == null) {
            throw new RegraNegocioException("Informe a forma de armazenamento.");
        }
        unidade = Textos.vazio(unidade) ? "UNIDADE" : unidade.trim().toUpperCase();
        if (estoqueMinimo == null) {
            estoqueMinimo = NIVEL_MINIMO_PADRAO;
        }
        if (estoqueMinimo < 0) {
            throw new RegraNegocioException("A quantidade mínima não pode ser negativa.");
        }
    }

    /** RF055: edicao do medicamento. */
    public void atualizarCom(Medicamento dados) {
        this.nome = dados.getNome();
        this.descricao = dados.getDescricao();
        this.fabricante = dados.getFabricante();
        this.armazenamento = dados.getArmazenamento();
        this.unidade = dados.getUnidade();
        this.estoqueMinimo = dados.getEstoqueMinimo();
    }

    // ---------------- getters e setters ----------------

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getFabricante() {
        return fabricante;
    }

    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    public Armazenamento getArmazenamento() {
        return armazenamento;
    }

    public void setArmazenamento(Armazenamento armazenamento) {
        this.armazenamento = armazenamento;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public Integer getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(Integer estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }
}
