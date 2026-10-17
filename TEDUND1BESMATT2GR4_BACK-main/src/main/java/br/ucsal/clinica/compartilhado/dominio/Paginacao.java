package br.ucsal.clinica.compartilhado.dominio;

/**
 * Pedido de pagina (parametros page e size da API - secao 4.2.3), sem depender do Spring.
 * O tamanho e limitado para evitar o trafego de grandes volumes de dados (RNF019).
 */
public record Paginacao(int pagina, int tamanho) {

    public static final int TAMANHO_PADRAO = 20;
    public static final int TAMANHO_MAXIMO = 100;

    public Paginacao {
        if (pagina < 0) {
            pagina = 0;
        }
        if (tamanho <= 0) {
            tamanho = TAMANHO_PADRAO;
        }
        if (tamanho > TAMANHO_MAXIMO) {
            tamanho = TAMANHO_MAXIMO;
        }
    }

    public static Paginacao de(Integer pagina, Integer tamanho) {
        return new Paginacao(pagina == null ? 0 : pagina, tamanho == null ? TAMANHO_PADRAO : tamanho);
    }
}
