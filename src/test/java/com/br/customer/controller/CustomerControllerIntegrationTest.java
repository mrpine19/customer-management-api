package com.br.customer.controller;

import com.br.customer.dtos.CustomerRequestDTO;
import com.br.customer.model.StatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class CustomerControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/customers";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("deve criar cliente com retorno 201 CREATED")
    void deveCriarClienteComRetorno201() throws Exception {
        CustomerRequestDTO request = new CustomerRequestDTO(
                "João Silva",
                "10111213141",
                "joao.silva@email.com",
                "11999990001",
                StatusEnum.ACTIVE,
                LocalDate.of(1990, 3, 12)
        );

        mockMvc.perform(post(BASE_URL)
                .header("Authorization", "Basic " + encodeCredentials(ADMIN_USERNAME, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("João Silva"))
                .andExpect(jsonPath("$.cpf").value("10111213141"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("deve recuperar cliente pelo ID com retorno 200 OK")
    void deveRecuperarClientePeloIdComRetorno200() throws Exception {
        mockMvc.perform(get(BASE_URL + "/1")
                .header("Authorization", "Basic " + encodeCredentials(ADMIN_USERNAME, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.cpf").exists());
    }

    @Test
    @DisplayName("deve retornar 404 ao buscar cliente inexistente")
    void deveRetornar404AoBuscarClienteInexistente() throws Exception {
        mockMvc.perform(get(BASE_URL + "/99999")
                .header("Authorization", "Basic " + encodeCredentials(ADMIN_USERNAME, ADMIN_PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deve atualizar cliente com retorno 200 OK")
    void deveAtualizarClienteComRetorno200() throws Exception {
        CustomerRequestDTO updateRequest = new CustomerRequestDTO(
                "João Silva Atualizado",
                "12345678901",
                "joao.novo@email.com",
                "11999990002",
                StatusEnum.INACTIVE,
                LocalDate.of(1990, 3, 12)
        );

        mockMvc.perform(put(BASE_URL + "/1")
                .header("Authorization", "Basic " + encodeCredentials(ADMIN_USERNAME, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("João Silva Atualizado"))
                .andExpect(jsonPath("$.email").value("joao.novo@email.com"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @DisplayName("deve deletar cliente com retorno 204 NO_CONTENT")
    void deveDeletarClienteComRetorno204() throws Exception {
        CustomerRequestDTO createRequest = new CustomerRequestDTO(
                "Cliente para Deletar",
                "21234567890",
                "deletar@email.com",
                "11999990003",
                StatusEnum.ACTIVE,
                LocalDate.of(1990, 1, 1)
        );

        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                .header("Authorization", "Basic " + encodeCredentials(ADMIN_USERNAME, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long clienteId = objectMapper.readTree(responseBody).get("id").asLong();

        mockMvc.perform(delete(BASE_URL + "/" + clienteId)
                .header("Authorization", "Basic " + encodeCredentials(ADMIN_USERNAME, ADMIN_PASSWORD)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/" + clienteId)
                .header("Authorization", "Basic " + encodeCredentials(ADMIN_USERNAME, ADMIN_PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("deve retornar 409 CONFLICT ao tentar criar cliente com CPF duplicado")
    void deveRetornar409AoCriarClienteComCpfDuplicado() throws Exception {
        CustomerRequestDTO firstRequest = new CustomerRequestDTO(
                "Primeiro Cliente",
                "20212223242",
                "primeiro@email.com",
                "11999990004",
                StatusEnum.ACTIVE,
                LocalDate.of(1990, 1, 1)
        );

        mockMvc.perform(post(BASE_URL)
                .header("Authorization", "Basic " + encodeCredentials(ADMIN_USERNAME, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        CustomerRequestDTO duplicateRequest = new CustomerRequestDTO(
                "Segundo Cliente",
                "20212223242",
                "segundo@email.com",
                "11999990005",
                StatusEnum.ACTIVE,
                LocalDate.of(1990, 1, 1)
        );

        mockMvc.perform(post(BASE_URL)
                .header("Authorization", "Basic " + encodeCredentials(ADMIN_USERNAME, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("deve retornar 400 BAD_REQUEST ao criar cliente com dados inválidos")
    void deveRetornar400AoCriarClienteComDadosInvalidos() throws Exception {
        String invalidRequest = "{\"name\": \"\", \"cpf\": \"invalid\"}";

        mockMvc.perform(post(BASE_URL)
                .header("Authorization", "Basic " + encodeCredentials(ADMIN_USERNAME, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("deve retornar todos os clientes com retorno 200 OK")
    void deveRetornarTodosOsClientesComRetorno200() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .header("Authorization", "Basic " + encodeCredentials(ADMIN_USERNAME, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    @DisplayName("deve filtrar clientes por status com retorno 200 OK")
    void deveFiltrarClientesPorStatusComRetorno200() throws Exception {
        mockMvc.perform(get(BASE_URL + "?status=ACTIVE")
                .header("Authorization", "Basic " + encodeCredentials(ADMIN_USERNAME, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("deve buscar clientes por nome com retorno 200 OK")
    void deveBuscarClientesPorNomeComRetorno200() throws Exception {
        mockMvc.perform(get(BASE_URL + "/search?name=Silva")
                .header("Authorization", "Basic " + encodeCredentials(ADMIN_USERNAME, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("deve retornar score do cliente com retorno 200 ou 404 ou 503 ou 504")
    void deveRetornarScoreDoClienteComRetornoValido() throws Exception {
        MvcResult result = mockMvc.perform(get(BASE_URL + "/1/score")
                .header("Authorization", "Basic " + encodeCredentials(ADMIN_USERNAME, ADMIN_PASSWORD)))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertThat(status).isIn(200, 404, 503, 504);
    }

    @Test
    @DisplayName("deve validar CPF com exatamente 11 dígitos na criação")
    void deveValidarCpfComExatamente11Digitos() throws Exception {
        CustomerRequestDTO requestWith10Digits = new CustomerRequestDTO(
                "Test Client",
                "1234567890",
                "test@email.com",
                "11999990006",
                StatusEnum.ACTIVE,
                LocalDate.of(1990, 1, 1)
        );

        mockMvc.perform(post(BASE_URL)
                .header("Authorization", "Basic " + encodeCredentials(ADMIN_USERNAME, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWith10Digits)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("deve validar e-mail no formato correto na criação")
    void deveValidarEmailCorretamente() throws Exception {
        CustomerRequestDTO requestWithInvalidEmail = new CustomerRequestDTO(
                "Test Client",
                "30415161718",
                "invalid-email",
                "11999990007",
                StatusEnum.ACTIVE,
                LocalDate.of(1990, 1, 1)
        );

        mockMvc.perform(post(BASE_URL)
                .header("Authorization", "Basic " + encodeCredentials(ADMIN_USERNAME, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWithInvalidEmail)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("deve validar que data de nascimento não pode ser no futuro")
    void deveValidarDataDeNascimentoNoFuturo() throws Exception {
        CustomerRequestDTO requestWithFutureBirthDate = new CustomerRequestDTO(
                "Test Client",
                "40516171819",
                "test@email.com",
                "11999990008",
                StatusEnum.ACTIVE,
                LocalDate.now().plusDays(1)
        );

        mockMvc.perform(post(BASE_URL)
                .header("Authorization", "Basic " + encodeCredentials(ADMIN_USERNAME, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWithFutureBirthDate)))
                .andExpect(status().isBadRequest());
    }

    private String encodeCredentials(String username, String password) {
        String auth = username + ":" + password;
        return Base64.getEncoder().encodeToString(auth.getBytes());
    }
}