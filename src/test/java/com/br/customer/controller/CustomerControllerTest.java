package com.br.customer.controller;

import com.br.customer.dtos.CustomerRequestDTO;
import com.br.customer.dtos.CustomerResponseDTO;
import com.br.customer.dtos.CustomerScoreResponseDTO;
import com.br.customer.exceptions.CustomerNotFoundException;
import com.br.customer.exceptions.DuplicateCpfException;
import com.br.customer.model.StatusEnum;
import com.br.score.dto.ScoreResponseDTO;
import com.br.score.service.CustomerScoreService;
import com.br.customer.service.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private CustomerScoreService customerScoreService;

    @InjectMocks
    private CustomerController customerController;

    private CustomerResponseDTO buildResponseDTO(Long id, String name, StatusEnum status) {
        return new CustomerResponseDTO(
                id, name, "12345678901", "email@test.com",
                "11999990000", status,
                LocalDate.of(1990, 1, 15),
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private CustomerRequestDTO buildRequestDTO(String name, String cpf, StatusEnum status) {
        return new CustomerRequestDTO(
                name, cpf, "email@test.com",
                "11999990000", status,
                LocalDate.of(1990, 1, 15)
        );
    }

    @Test
    @DisplayName("deve retornar lista de clientes quando status é nulo (sem filtro)")
    void deveRetornarTodosOsClientesQuandoStatusForNulo() {
        List<CustomerResponseDTO> expected = List.of(
                buildResponseDTO(1L, "Alice", StatusEnum.ACTIVE),
                buildResponseDTO(2L, "Bob", StatusEnum.INACTIVE)
        );
        when(customerService.getAllCustomers(null)).thenReturn(expected);

        List<CustomerResponseDTO> result = customerController.getAllCustomers(null);

        assertThat(result).hasSize(2).isEqualTo(expected);
        verify(customerService).getAllCustomers(null);
    }

    @Test
    @DisplayName("deve retornar apenas clientes ativos quando status=ACTIVE é informado")
    void deveRetornarClientesFiltradosPorStatus() {
        List<CustomerResponseDTO> expected = List.of(
                buildResponseDTO(1L, "Alice", StatusEnum.ACTIVE)
        );
        when(customerService.getAllCustomers(StatusEnum.ACTIVE)).thenReturn(expected);

        List<CustomerResponseDTO> result = customerController.getAllCustomers(StatusEnum.ACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(StatusEnum.ACTIVE);
        verify(customerService).getAllCustomers(StatusEnum.ACTIVE);
    }

    @Test
    @DisplayName("deve retornar lista vazia quando não há clientes cadastrados")
    void deveRetornarListaVaziaQuandoNaoHaClientes() {
        when(customerService.getAllCustomers(null)).thenReturn(Collections.emptyList());

        List<CustomerResponseDTO> result = customerController.getAllCustomers(null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deve retornar o cliente quando o id é válido")
    void deveRetornarClienteQuandoIdValido() {
        CustomerResponseDTO expected = buildResponseDTO(1L, "Alice", StatusEnum.ACTIVE);
        when(customerService.getCustomerById(1L)).thenReturn(expected);

        CustomerResponseDTO result = customerController.getCustomerById(1L);

        assertThat(result).isEqualTo(expected);
        assertThat(result.id()).isEqualTo(1L);
        verify(customerService).getCustomerById(1L);
    }

    @Test
    @DisplayName("deve lançar CustomerNotFoundException quando o id não é encontrado")
    void deveLancarExcecaoQuandoIdNaoEncontrado() {
        when(customerService.getCustomerById(99L))
                .thenThrow(new CustomerNotFoundException("Customer not found with id: 99"));

        assertThatThrownBy(() -> customerController.getCustomerById(99L))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("deve retornar o score do cliente quando o id é válido")
    void deveRetornarScoreDoClienteQuandoIdValido() {
        ScoreResponseDTO score = new ScoreResponseDTO("12345678901", 750, "GOOD");
        CustomerScoreResponseDTO expected = new CustomerScoreResponseDTO(
                1L, "Alice", "12345678901", "email@test.com", "ACTIVE", score
        );
        when(customerScoreService.getCustomerScoreById(1L)).thenReturn(expected);

        CustomerScoreResponseDTO result = customerController.getCustomerScoreById(1L);

        assertThat(result).isEqualTo(expected);
        assertThat(result.score().score()).isEqualTo(750);
        verify(customerScoreService).getCustomerScoreById(1L);
    }

    @Test
    @DisplayName("deve lançar CustomerNotFoundException quando cliente do score não é encontrado")
    void deveLancarExcecaoQuandoClienteDoScoreNaoEncontrado() {
        when(customerScoreService.getCustomerScoreById(99L))
                .thenThrow(new CustomerNotFoundException("Customer not found with id: 99"));
        assertThatThrownBy(() -> customerController.getCustomerScoreById(99L))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("deve criar e retornar o cliente quando os dados são válidos")
    void deveCriarClienteComSucesso() {
        CustomerRequestDTO request = buildRequestDTO("Alice", "12345678901", StatusEnum.ACTIVE);
        CustomerResponseDTO expected = buildResponseDTO(1L, "Alice", StatusEnum.ACTIVE);
        when(customerService.createCustomer(request)).thenReturn(expected);

        CustomerResponseDTO result = customerController.createCustomer(request);

        assertThat(result).isEqualTo(expected);
        assertThat(result.id()).isEqualTo(1L);
        verify(customerService).createCustomer(request);
    }

    @Test
    @DisplayName("deve lançar DuplicateCpfException quando CPF já está cadastrado")
    void deveLancarExcecaoQuandoCpfDuplicado() {
        CustomerRequestDTO request = buildRequestDTO("Alice", "12345678901", StatusEnum.ACTIVE);
        when(customerService.createCustomer(request))
                .thenThrow(new DuplicateCpfException("12345678901"));

        assertThatThrownBy(() -> customerController.createCustomer(request))
                .isInstanceOf(DuplicateCpfException.class)
                .hasMessageContaining("12345678901");
    }

    @Test
    @DisplayName("deve atualizar e retornar o cliente quando os dados são válidos")
    void deveAtualizarClienteComSucesso() {
        CustomerRequestDTO request = buildRequestDTO("Alice Atualizada", "12345678901", StatusEnum.ACTIVE);
        CustomerResponseDTO expected = buildResponseDTO(1L, "Alice Atualizada", StatusEnum.ACTIVE);
        when(customerService.updateCustomer(1L, request)).thenReturn(expected);

        CustomerResponseDTO result = customerController.updateCustomer(1L, request);

        assertThat(result.name()).isEqualTo("Alice Atualizada");
        verify(customerService).updateCustomer(1L, request);
    }

    @Test
    @DisplayName("deve lançar CustomerNotFoundException ao atualizar cliente inexistente")
    void deveLancarExcecaoAoAtualizarClienteInexistente() {
        CustomerRequestDTO request = buildRequestDTO("Ghost", "12345678902", StatusEnum.ACTIVE);
        when(customerService.updateCustomer(99L, request))
                .thenThrow(new CustomerNotFoundException("Customer not found with id: 99"));

        assertThatThrownBy(() -> customerController.updateCustomer(99L, request))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("deve lançar DuplicateCpfException ao atualizar com CPF de outro cliente")
    void deveLancarExcecaoAoAtualizarComCpfDuplicado() {
        CustomerRequestDTO request = buildRequestDTO("Alice", "99999999999", StatusEnum.ACTIVE);
        when(customerService.updateCustomer(1L, request))
                .thenThrow(new DuplicateCpfException("99999999999"));

        assertThatThrownBy(() -> customerController.updateCustomer(1L, request))
                .isInstanceOf(DuplicateCpfException.class)
                .hasMessageContaining("99999999999");
    }

    @Test
    @DisplayName("deve deletar o cliente sem retorno quando o id é válido")
    void deveDeletarClienteComSucesso() {
        doNothing().when(customerService).deleteCustomerById(1L);

        customerController.deleteCustomerById(1L);

        verify(customerService, times(1)).deleteCustomerById(1L);
    }

    @Test
    @DisplayName("deve lançar CustomerNotFoundException ao deletar cliente inexistente")
    void deveLancarExcecaoAoDeletarClienteInexistente() {
        doThrow(new CustomerNotFoundException("Customer not found with id: 99"))
                .when(customerService).deleteCustomerById(99L);

        assertThatThrownBy(() -> customerController.deleteCustomerById(99L))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("deve retornar clientes que correspondem ao nome pesquisado")
    void deveRetornarClientesQuandoNomeCorresponde() {
        List<CustomerResponseDTO> expected = List.of(
                buildResponseDTO(1L, "Alice Silva", StatusEnum.ACTIVE)
        );
        when(customerService.searchByName("Alice")).thenReturn(expected);

        List<CustomerResponseDTO> result = customerController.searchByName("Alice");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Alice Silva");
        verify(customerService).searchByName("Alice");
    }

    @Test
    @DisplayName("deve retornar lista vazia quando nenhum cliente corresponde ao nome")
    void deveRetornarListaVaziaQuandoNenhumClienteCorresponde() {
        when(customerService.searchByName("NomeInexistente")).thenReturn(Collections.emptyList());

        List<CustomerResponseDTO> result = customerController.searchByName("NomeInexistente");

        assertThat(result).isEmpty();
        verify(customerService).searchByName("NomeInexistente");
    }

    @Test
    @DisplayName("deve pesquisar com string vazia e retornar todos os clientes")
    void devePesquisarComStringVaziaRetornandoTodos() {
        List<CustomerResponseDTO> expected = List.of(
                buildResponseDTO(1L, "Alice", StatusEnum.ACTIVE),
                buildResponseDTO(2L, "Bob", StatusEnum.INACTIVE)
        );
        when(customerService.searchByName("")).thenReturn(expected);

        List<CustomerResponseDTO> result = customerController.searchByName("");

        assertThat(result).hasSize(2);
        verify(customerService).searchByName("");
    }
}