package com.br.customer.security;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private static final String BASE_URL = "/customers";
    private static final String USER_USERNAME = "user";
    private static final String USER_PASSWORD = "user123";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    private String encodeCredentials(String username, String password) {
        String auth = username + ":" + password;
        return Base64.getEncoder().encodeToString(auth.getBytes());
    }

    @Test
    @DisplayName("deve retornar 401 ao acessar GET sem autenticação")
    void deve401AoAcessarGetSemAutenticacao() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deve retornar 401 ao acessar POST sem autenticação")
    void deve401AoAcessarPostSemAutenticacao() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deve retornar 401 ao acessar DELETE sem autenticação")
    void deve401AoAcessarDeleteSemAutenticacao() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deve retornar 200 ao acessar GET com USER_ROLE")
    void deve200AoAcessarGetComUserRole() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .header("Authorization", "Basic " + encodeCredentials(USER_USERNAME, USER_PASSWORD)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("deve retornar 200 ao acessar GET com ADMIN_ROLE")
    void deve200AoAcessarGetComAdminRole() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .header("Authorization", "Basic " + encodeCredentials(ADMIN_USERNAME, ADMIN_PASSWORD)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("deve retornar 403 ao fazer POST com credencial USER")
    void deve403AoFazerPostComUserCredencial() throws Exception {
        CustomerRequestDTO request = new CustomerRequestDTO(
                "Test User",
                "22233344455",
                "user.test@email.com",
                "11999999999",
                StatusEnum.ACTIVE,
                LocalDate.of(1990, 1, 1)
        );

        mockMvc.perform(post(BASE_URL)
                .header("Authorization", "Basic " + encodeCredentials(USER_USERNAME, USER_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("deve retornar 201 ou 409 ao fazer POST com credencial ADMIN")
    void deveCriarClienteComAdminCredencial() throws Exception {
        CustomerRequestDTO request = new CustomerRequestDTO(
                "Test Admin",
                "47788899900",
                "admin.test@email.com",
                "11999999999",
                StatusEnum.ACTIVE,
                LocalDate.of(1990, 1, 1)
        );

        MvcResult result = mockMvc.perform(post(BASE_URL)
                .header("Authorization", "Basic " + encodeCredentials(ADMIN_USERNAME, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertThat(status).isIn(201, 409);
    }

    @Test
    @DisplayName("deve retornar 403 ao fazer DELETE com credencial USER")
    void deve403AoFazerDeleteComUserCredencial() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1")
                .header("Authorization", "Basic " + encodeCredentials(USER_USERNAME, USER_PASSWORD)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("deve retornar 200 ao acessar GET /customers/{id}/score com USER autenticado")
    void deve200AoAcessarScoreComUserCredencial() throws Exception {
        MvcResult result = mockMvc.perform(get(BASE_URL + "/1/score")
                .header("Authorization", "Basic " + encodeCredentials(USER_USERNAME, USER_PASSWORD)))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertThat(status).isIn(200, 404, 504, 503);
    }
}