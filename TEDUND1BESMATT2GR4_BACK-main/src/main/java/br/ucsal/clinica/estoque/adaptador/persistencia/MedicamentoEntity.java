package br.ucsal.clinica.estoque.adaptador.persistencia;

import br.ucsal.clinica.auditoria.Rotulavel;
import br.ucsal.clinica.compartilhado.adaptador.persistencia.EntidadeJpa;
import br.ucsal.clinica.estoque.dominio.Armazenamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Mapeamento JPA do medicamento (tb_medicamento). A coluna versao (@Version herdada)
 * e incrementada a cada movimentacao: e o bloqueio otimista do estoque (DA013).
 */
@Getter
@Setter
@Entity
@Table(name = "tb_medicamento")
public class MedicamentoEntity extends EntidadeJpa implements Rotulavel {

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, columnDefinition = "text")
    private String descricao;

    @Column(nullable = false, length = 150)
    private String fabricante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private Armazenamento armazenamento;

    @Column(nullable = false, length = 20)
    private String unidade = "UNIDADE";

    @Column(name = "estoque_minimo", nullable = false)
    private Integer estoqueMinimo = 10;

    @Override
    public String rotuloAuditoria() {
        return nome;
    }
}
