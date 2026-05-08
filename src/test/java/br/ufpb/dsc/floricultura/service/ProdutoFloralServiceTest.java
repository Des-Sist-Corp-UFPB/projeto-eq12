package br.ufpb.dsc.floricultura.service;

import br.ufpb.dsc.floricultura.domain.CategoriaProdutoFloral;
import br.ufpb.dsc.floricultura.domain.ProdutoFloral;
import br.ufpb.dsc.floricultura.dto.ProdutoFloralForm;
import br.ufpb.dsc.floricultura.exception.ProdutoFloralNaoEncontradoException;
import br.ufpb.dsc.floricultura.repository.ProdutoFloralRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link ProdutoFloralService}.
 *
 * <p><strong>Testes Unitários vs Testes de Integração:</strong>
 * <ul>
 *   <li><strong>Unitário</strong>: testa uma classe isolada, substituindo suas dependências
 *       por objetos falsos (mocks). Rápido, sem banco de dados.</li>
 *   <li><strong>Integração</strong>: testa múltiplas camadas juntas com infraestrutura real
 *       (banco, HTTP, etc.). Mais lento, mais realista.</li>
 * </ul>
 *
 * <p><strong>Mockito:</strong><br>
 * Mockito é o framework de mocking mais usado no ecossistema Spring.
 * Permite criar objetos "falsos" que simulam o comportamento das dependências:
 * <ul>
 *   <li>{@code @Mock} — cria um mock da classe/interface.</li>
 *   <li>{@code @InjectMocks} — cria a classe sob teste e injeta os mocks nela.</li>
 *   <li>{@code when(...).thenReturn(...)} — configura o comportamento do mock.</li>
 *   <li>{@code verify(...)} — verifica se um método foi chamado.</li>
 * </ul>
 *
 * <p><strong>AssertJ:</strong><br>
 * Biblioteca de asserções fluentes incluída no Spring Boot Test.
 * Mais legível que o JUnit assertions padrão:
 * {@code assertThat(resultado).isNotNull().hasFieldOrPropertyWithValue("nome", "Arroz")}
 *
 * @author DSC - UFPB Campus IV
 */
@ExtendWith(MockitoExtension.class) // Ativa o suporte ao Mockito no JUnit 5
@DisplayName("ProdutoFloralService — Testes Unitários")
class ProdutoFloralServiceTest {

    // @Mock cria um objeto falso que simula o ProdutoFloralRepository
    // Nenhuma consulta real ao banco é feita — tudo é simulado
    @Mock
    private ProdutoFloralRepository produtoFloralRepository;

    // @InjectMocks cria uma instância real do ProdutoFloralService
    // e injeta automaticamente o @Mock acima no construtor
    @InjectMocks
    private ProdutoFloralService produtoFloralService;

    // Dados de teste compartilhados
    private ProdutoFloral produtoFloralExistente;
    private ProdutoFloralForm formValido;

    /**
     * Configuração executada antes de cada teste.
     * {@code @BeforeEach} garante um estado limpo e previsível para cada teste.
     */
    @BeforeEach
    void setUp() {
        produtoFloralExistente = new ProdutoFloral("Rosa Vermelha", "Flor de corte para buques",
                new BigDecimal("12.90"), CategoriaProdutoFloral.FLOR_CORTE, "Vermelha", 24);
        produtoFloralExistente.setId(1L);

        formValido = new ProdutoFloralForm("Orquidea Phalaenopsis", "Orquidea em vaso",
                new BigDecimal("89.90"), CategoriaProdutoFloral.PLANTA, "Branca", 8);
    }

    // =========================================================================
    // TESTES: buscarPorId
    // =========================================================================

    @Test
    @DisplayName("buscarPorId: deve retornar produto quando ID existe")
    void buscarPorId_quandoIdExiste_deveRetornarProduto() {
        // GIVEN (Arrange) — configura o comportamento do mock
        // "Quando findById(1L) for chamado, retorne o produto de teste"
        when(produtoFloralRepository.findById(1L)).thenReturn(Optional.of(produtoFloralExistente));

        // WHEN (Act) — executa o método sob teste
        ProdutoFloral resultado = produtoFloralService.buscarPorId(1L);

        // THEN (Assert) — verifica o resultado
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNome()).isEqualTo("Rosa Vermelha");

        // Verifica que o repositório foi chamado exatamente uma vez com o ID correto
        verify(produtoFloralRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("buscarPorId: deve lançar exceção quando ID não existe")
    void buscarPorId_quandoIdNaoExiste_deveLancarExcecao() {
        // GIVEN
        when(produtoFloralRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN + THEN — assertThatThrownBy verifica que a exceção é lançada
        assertThatThrownBy(() -> produtoFloralService.buscarPorId(99L))
                .isInstanceOf(ProdutoFloralNaoEncontradoException.class)
                .hasMessageContaining("99");

        verify(produtoFloralRepository, times(1)).findById(99L);
    }

    // =========================================================================
    // TESTES: criar
    // =========================================================================

    @Test
    @DisplayName("criar: deve salvar e retornar o novo produto")
    void criar_comFormValido_deveSalvarERetornarProduto() {
        // GIVEN
        // Simula o save() retornando um produto com ID gerado pelo banco
        ProdutoFloral produtoFloralSalvo = new ProdutoFloral(formValido.nome(), formValido.descricao(), formValido.preco(),
                formValido.categoria(), formValido.cor(), formValido.quantidadeEstoque());
        produtoFloralSalvo.setId(2L);
        when(produtoFloralRepository.save(any(ProdutoFloral.class))).thenReturn(produtoFloralSalvo);

        // WHEN
        ProdutoFloral resultado = produtoFloralService.criar(formValido);

        // THEN
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(2L);
        assertThat(resultado.getNome()).isEqualTo("Orquidea Phalaenopsis");
        assertThat(resultado.getPreco()).isEqualByComparingTo("89.90");
        assertThat(resultado.getCategoria()).isEqualTo(CategoriaProdutoFloral.PLANTA);
        assertThat(resultado.getCor()).isEqualTo("Branca");
        assertThat(resultado.getQuantidadeEstoque()).isEqualTo(8);

        // Verifica que save() foi chamado com qualquer ProdutoFloral (não importa qual instância)
        verify(produtoFloralRepository, times(1)).save(any(ProdutoFloral.class));
    }

    // =========================================================================
    // TESTES: atualizar
    // =========================================================================

    @Test
    @DisplayName("atualizar: deve modificar os dados do produto existente")
    void atualizar_quandoProdutoExiste_deveAtualizarDados() {
        // GIVEN
        when(produtoFloralRepository.findById(1L)).thenReturn(Optional.of(produtoFloralExistente));
        when(produtoFloralRepository.save(any(ProdutoFloral.class))).thenReturn(produtoFloralExistente);

        ProdutoFloralForm formAtualizado = new ProdutoFloralForm("Buque Primavera", "Arranjo com flores do campo",
                new BigDecimal("59.90"), CategoriaProdutoFloral.ARRANJO, "Colorido", 5);

        // WHEN
        ProdutoFloral resultado = produtoFloralService.atualizar(1L, formAtualizado);

        // THEN
        assertThat(resultado.getNome()).isEqualTo("Buque Primavera");
        assertThat(resultado.getPreco()).isEqualByComparingTo("59.90");
        assertThat(resultado.getCategoria()).isEqualTo(CategoriaProdutoFloral.ARRANJO);
        assertThat(resultado.getQuantidadeEstoque()).isEqualTo(5);

        verify(produtoFloralRepository).findById(1L);
        verify(produtoFloralRepository).save(any(ProdutoFloral.class));
    }

    @Test
    @DisplayName("atualizar: deve lançar exceção quando produto não existe")
    void atualizar_quandoProdutoNaoExiste_deveLancarExcecao() {
        // GIVEN
        when(produtoFloralRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN + THEN
        assertThatThrownBy(() -> produtoFloralService.atualizar(99L, formValido))
                .isInstanceOf(ProdutoFloralNaoEncontradoException.class);

        // Verifica que save() NUNCA foi chamado (produto não existe, não deve salvar)
        verify(produtoFloralRepository, never()).save(any());
    }

    // =========================================================================
    // TESTES: excluir
    // =========================================================================

    @Test
    @DisplayName("excluir: deve deletar produto quando ID existe")
    void excluir_quandoProdutoExiste_deveDeletar() {
        // GIVEN
        when(produtoFloralRepository.existsById(1L)).thenReturn(true);
        // doNothing() é o padrão para void, mas declaramos explicitamente para clareza
        doNothing().when(produtoFloralRepository).deleteById(1L);

        // WHEN — não deve lançar exceção
        assertThatCode(() -> produtoFloralService.excluir(1L))
                .doesNotThrowAnyException();

        // THEN
        verify(produtoFloralRepository).existsById(1L);
        verify(produtoFloralRepository).deleteById(1L);
    }

    @Test
    @DisplayName("excluir: deve lançar exceção quando produto não existe")
    void excluir_quandoProdutoNaoExiste_deveLancarExcecao() {
        // GIVEN
        when(produtoFloralRepository.existsById(99L)).thenReturn(false);

        // WHEN + THEN
        assertThatThrownBy(() -> produtoFloralService.excluir(99L))
                .isInstanceOf(ProdutoFloralNaoEncontradoException.class)
                .hasMessageContaining("99");

        // deleteById NUNCA deve ser chamado se o produto não existe
        verify(produtoFloralRepository, never()).deleteById(any());
    }
}
