package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.domain.CategoriaProdutoFloral;
import br.ufpb.dsc.mercado.domain.ProdutoFloral;
import br.ufpb.dsc.mercado.repository.ProdutoFloralRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração do {@link ProdutoFloralController} com Testcontainers.
 *
 * <p><strong>Testcontainers:</strong><br>
 * Testcontainers é uma biblioteca Java que permite usar containers Docker nos testes.
 * Em vez de mockar o banco de dados, subimos um PostgreSQL real em um container Docker,
 * garantindo que os testes reflitam o comportamento de produção.
 *
 * <p>Pré-requisito: Docker deve estar rodando na máquina que executa os testes.
 *
 * <p><strong>Anotações de teste usadas:</strong>
 * <ul>
 *   <li>{@code @SpringBootTest} — carrega o contexto completo do Spring</li>
 *   <li>{@code @AutoConfigureMockMvc} — configura o MockMvc automaticamente</li>
 *   <li>{@code @Testcontainers} — ativa o suporte ao Testcontainers no JUnit 5</li>
 *   <li>{@code @ActiveProfiles("test")} — usa as propriedades de application-test.yml</li>
 * </ul>
 *
 * <p><strong>MockMvc:</strong><br>
 * MockMvc simula requisições HTTP sem precisar subir um servidor real.
 * Permite testar controllers de forma integrada com o Spring MVC.
 *
 * <p><strong>{@code @WithMockUser}:</strong><br>
 * Simula um usuário autenticado sem precisar passar pelo processo real de login.
 * Necessário porque todos os endpoints de /produtos-florais exigem autenticação.
 *
 * @author DSC - UFPB Campus IV
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers // Ativa o gerenciamento automático dos containers pelo JUnit 5
@ActiveProfiles("test")
@DisplayName("ProdutoFloralController — Testes de Integração")
class ProdutoFloralControllerTest {

    /**
     * Container PostgreSQL gerenciado pelo Testcontainers.
     *
     * <p>{@code @Container} indica ao JUnit 5 (via Testcontainers) que este campo
     * é um container que deve ser iniciado antes dos testes e parado ao final.
     *
     * <p>{@code @ServiceConnection} (Spring Boot 3.1+) configura automaticamente o
     * DataSource da aplicação para apontar para este container, sem precisar
     * sobrescrever properties manualmente.
     *
     * <p>{@code static} faz o container ser compartilhado entre todos os testes da classe
     * (mais eficiente — container sobe apenas uma vez).
     */
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc; // Simula requisições HTTP

    @Autowired
    private ProdutoFloralRepository produtoFloralRepository; // Para preparar dados de teste

    private ProdutoFloral produtoFloralCadastrado;

    /**
     * Configuração executada antes de cada teste.
     * Limpa o banco e insere dados de teste para garantir isolamento entre testes.
     */
    @BeforeEach
    void setUp() {
        // Limpa o banco antes de cada teste para evitar interferência entre eles
        produtoFloralRepository.deleteAll();

        // Cria um produto de teste para os cenários que precisam de dado existente
        produtoFloralCadastrado = produtoFloralRepository.save(
                new ProdutoFloral("Rosa Vermelha", "Flor de corte para buques",
                        new BigDecimal("12.90"), CategoriaProdutoFloral.FLOR_CORTE, "Vermelha", 24)
        );
    }

    // =========================================================================
    // TESTES: GET /produtos-florais
    // =========================================================================

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN") // Simula usuário autenticado
    @DisplayName("GET /produtos-florais: deve retornar página de listagem com status 200")
    void listar_usuarioAutenticado_deveRetornarPaginaLista() throws Exception {
        mockMvc.perform(get("/produtos-florais"))
                // Verifica o status HTTP 200 OK
                .andExpect(status().isOk())
                // Verifica que a view retornada é a correta
                .andExpect(view().name("produtos-florais/lista"))
                // Verifica que o model contém o atributo "produtos"
                .andExpect(model().attributeExists("produtosFlorais"))
                // Verifica que o HTML contém o nome do produto cadastrado
                .andExpect(content().string(containsString("Rosa Vermelha")));
    }

    @Test
    @DisplayName("GET /produtos-florais: usuário não autenticado deve ser redirecionado para /login")
    void listar_semAutenticacao_deveRedirecionarParaLogin() throws Exception {
        mockMvc.perform(get("/produtos-florais"))
                // Spring Security redireciona (302) para a página de login
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // =========================================================================
    // TESTES: GET /produtos-florais/novo
    // =========================================================================

    @Test
    @WithMockUser
    @DisplayName("GET /produtos-florais/novo: deve retornar fragmento do formulário vazio")
    void novoForm_deveRetornarFragmentoFormulario() throws Exception {
        mockMvc.perform(get("/produtos-florais/novo"))
                .andExpect(status().isOk())
                .andExpect(view().name("produtos-florais/fragments/form :: modal"))
                .andExpect(model().attributeExists("form"))
                // produto null indica modo de criação
                .andExpect(model().attribute("produtoFloral", nullValue()));
    }

    // =========================================================================
    // TESTES: POST /produtos-florais
    // =========================================================================

    @Test
    @WithMockUser
    @DisplayName("POST /produtos-florais: deve criar produto e retornar fragmento da linha")
    void criar_dadosValidos_deveCriarERetornarLinha() throws Exception {
        mockMvc.perform(post("/produtos-florais")
                        // csrf() adiciona o token CSRF para não falhar na proteção CSRF
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nome", "Orquidea Phalaenopsis")
                        .param("descricao", "Orquidea em vaso")
                        .param("preco", "89.90")
                        .param("categoria", "PLANTA")
                        .param("cor", "Branca")
                        .param("quantidadeEstoque", "8"))
                .andExpect(status().isOk())
                .andExpect(view().name("produtos-florais/fragments/linha :: linha"))
                .andExpect(model().attributeExists("produtoFloral"))
                // O HTML retornado deve conter o nome do produto criado
                .andExpect(content().string(containsString("Orquidea Phalaenopsis")));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /produtos-florais: dados inválidos devem retornar formulário com erros")
    void criar_dadosInvalidos_deveRetornarFormularioComErros() throws Exception {
        mockMvc.perform(post("/produtos-florais")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nome", "") // Nome vazio — inválido!
                        .param("preco", "-1.00") // Preço negativo — inválido!
                        .param("categoria", "FLOR_CORTE")
                        .param("quantidadeEstoque", "-1"))
                .andExpect(status().isOk())
                // Retorna o formulário com erros em vez de criar o produto
                .andExpect(view().name("produtos-florais/fragments/form :: modal"))
                // O model deve ter erros de validação
                .andExpect(model().hasErrors());
    }

    // =========================================================================
    // TESTES: GET /produtos-florais/{id}/editar
    // =========================================================================

    @Test
    @WithMockUser
    @DisplayName("GET /produtos-florais/{id}/editar: deve retornar formulário preenchido")
    void editarForm_produtoExistente_deveRetornarFormularioPreenchido() throws Exception {
        mockMvc.perform(get("/produtos-florais/{id}/editar", produtoFloralCadastrado.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("produtos-florais/fragments/form :: modal"))
                .andExpect(model().attributeExists("form", "produtoFloral"))
                .andExpect(content().string(containsString("Rosa Vermelha")));
    }

    // =========================================================================
    // TESTES: DELETE /produtos-florais/{id}
    // =========================================================================

    @Test
    @WithMockUser
    @DisplayName("DELETE /produtos-florais/{id}: deve excluir produto e retornar 200")
    void excluir_produtoExistente_deveRetornar200() throws Exception {
        mockMvc.perform(delete("/produtos-florais/{id}", produtoFloralCadastrado.getId())
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /produtos-florais/{id}: produto inexistente deve retornar 404")
    void excluir_produtoInexistente_deveRetornar404() throws Exception {
        mockMvc.perform(delete("/produtos-florais/{id}", 9999L)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
