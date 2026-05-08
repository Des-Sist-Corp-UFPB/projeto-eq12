package br.ufpb.dsc.mercado.exception;

/**
 * Excecao lancada quando um produto floral nao e encontrado no banco de dados.
 *
 * <p><strong>Por que usar excecoes especificas de dominio?</strong><br>
 * Em vez de lancar uma excecao generica ({@code RuntimeException} com mensagem "not found"),
 * criar excecoes especificas melhora:
 * <ul>
 *   <li><strong>Legibilidade</strong>: o codigo comunica claramente o que aconteceu.</li>
 *   <li><strong>Tratamento centralizado</strong>: e possivel capturar esta excecao especifica
 *       em um handler global ({@code @ControllerAdvice}) e retornar HTTP 404.</li>
 *   <li><strong>Rastreabilidade</strong>: logs ficam mais descritivos.</li>
 * </ul>
 *
 * <p><strong>Por que extends RuntimeException?</strong><br>
 * {@code RuntimeException} e uma "unchecked exception" - nao precisa ser declarada no {@code throws}
 * nem capturada obrigatoriamente. E a abordagem moderna em Spring para excecoes de negocio.
 * "Checked exceptions" (que extends {@code Exception}) sao mais usadas para erros recuperaveis
 * de infraestrutura (I/O, rede, etc.).
 *
 * @author DSC - UFPB Campus IV
 */
public class ProdutoFloralNaoEncontradoException extends RuntimeException {

    /**
     * Cria uma excecao com mensagem padrao informando o ID do produto floral nao encontrado.
     *
     * @param id identificador do produto floral que nao foi encontrado
     */
    public ProdutoFloralNaoEncontradoException(Long id) {
        super("Produto floral nao encontrado com id: " + id);
    }

    /**
     * Cria uma excecao com mensagem customizada.
     *
     * @param mensagem descricao do erro
     */
    public ProdutoFloralNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
