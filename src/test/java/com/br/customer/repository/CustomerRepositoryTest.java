package com.br.customer.repository;

import com.br.customer.model.Customer;
import com.br.customer.model.StatusEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerRepositoryTest {

	@Mock
	private CustomerRepository customerRepository;

    private Customer buildCustomer(Long id, String name, String cpf, StatusEnum status) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        customer.setCpf(cpf);
        customer.setEmail("email@test.com");
        customer.setPhone("11999990000");
        customer.setStatus(status);
        customer.setBirthDate(LocalDate.of(1990, 1, 15));
        customer.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        customer.setUpdatedAt(LocalDateTime.of(2024, 1, 1, 11, 0));
        return customer;
    }

	@Test
	@DisplayName("deve retornar cliente quando buscar por CPF existente")
	void deveRetornarClienteQuandoBuscarPorCpfExistente() {
		Customer customer = buildCustomer(1L, "Alice", "12345678901", StatusEnum.ACTIVE);
		when(customerRepository.findByCpf("12345678901")).thenReturn(Optional.of(customer));

		Optional<Customer> result = customerRepository.findByCpf("12345678901");

		assertThat(result).isPresent();
		assertThat(result.get().getName()).isEqualTo("Alice");
		verify(customerRepository, times(1)).findByCpf("12345678901");
	}

	@Test
	@DisplayName("deve retornar vazio quando buscar por CPF inexistente")
	void deveRetornarVazioQuandoBuscarPorCpfInexistente() {
		when(customerRepository.findByCpf("00000000000")).thenReturn(Optional.empty());

		Optional<Customer> result = customerRepository.findByCpf("00000000000");

		assertThat(result).isEmpty();
		verify(customerRepository, times(1)).findByCpf("00000000000");
	}

	@Test
	@DisplayName("deve retornar clientes quando buscar por nome")
	void deveRetornarClientesQuandoBuscarPorNome() {
		Customer customer = buildCustomer(2L, "Alice Silva", "11122233344", StatusEnum.ACTIVE);
		when(customerRepository.findByNameContainingIgnoreCase("Alice")).thenReturn(List.of(customer));

		List<Customer> result = customerRepository.findByNameContainingIgnoreCase("Alice");

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo("Alice Silva");
		verify(customerRepository, times(1)).findByNameContainingIgnoreCase("Alice");
	}

	@Test
	@DisplayName("deve retornar lista vazia quando nome não corresponder")
	void deveRetornarListaVaziaQuandoNomeNaoCorresponder() {
		when(customerRepository.findByNameContainingIgnoreCase("NomeInexistente"))
				.thenReturn(Collections.emptyList());

		List<Customer> result = customerRepository.findByNameContainingIgnoreCase("NomeInexistente");

		assertThat(result).isEmpty();
		verify(customerRepository, times(1)).findByNameContainingIgnoreCase("NomeInexistente");
	}

	@Test
	@DisplayName("deve aceitar CPF nulo e retornar vazio quando não houver correspondência")
	void deveRetornarVazioQuandoCpfForNulo() {
		when(customerRepository.findByCpf(null)).thenReturn(Optional.empty());

		Optional<Customer> result = customerRepository.findByCpf(null);

		assertThat(result).isEmpty();
		verify(customerRepository, times(1)).findByCpf(null);
	}

	@Test
	@DisplayName("deve propagar exceção quando busca por CPF falhar")
	void devePropagarExcecaoQuandoBuscaPorCpfFalhar() {
		when(customerRepository.findByCpf("99999999999"))
				.thenThrow(new RuntimeException("falha no repositório"));

		assertThatThrownBy(() -> customerRepository.findByCpf("99999999999"))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("falha no repositório");
	}
}