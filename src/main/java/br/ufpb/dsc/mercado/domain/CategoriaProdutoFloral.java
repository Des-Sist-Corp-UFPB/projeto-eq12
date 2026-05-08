package br.ufpb.dsc.mercado.domain;

/**
 * Categorias simples para organizar o catalogo de uma floricultura.
 */
public enum CategoriaProdutoFloral {

    FLOR_CORTE("Flor de corte"),
    ARRANJO("Arranjo"),
    PLANTA("Planta"),
    PRESENTE("Presente"),
    ACESSORIO("Acessorio");

    private final String descricao;

    CategoriaProdutoFloral(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
