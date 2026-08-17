package com.br.score.service;

import com.br.customer.dtos.CustomerScoreResponseDTO;
import com.br.customer.exceptions.CustomerNotFoundException;
import com.br.customer.model.Customer;
import com.br.customer.model.StatusEnum;
import com.br.customer.service.CustomerService;
import com.br.score.adapter.ScoreClientAdapter;
import com.br.score.dto.ScoreResponseDTO;
import com.br.score.exceptions.ScoreServiceUnavailableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerScoreServiceTest {

	@Mock
	private CustomerService customerService;

	@Mock
	private ScoreClientAdapter scoreClientAdapter;

	@InjectMocks
	private CustomerScoreService customerScoreService;

    private Customer buildCustomer(Long id, String name, String cpf, String email, StatusEnum status) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        customer.setCpf(cpf);
        customer.setEmail(email);
        customer.setStatus(status);
        return customer;
    }

    @Test
	@DisplayName("deve retornar score do cliente quando id for válido")
	void deveRetornarScoreDoClienteQuandoIdValido() {
		Long id = 1L;
		Customer customer = buildCustomer(id, "Alice", "12345678901", "alice@test.com", StatusEnum.ACTIVE);
		ScoreResponseDTO score = new ScoreResponseDTO("12345678901", 750, "GOOD");

		when(customerService.findCustomerById(id)).thenReturn(customer);
		when(scoreClientAdapter.getScoreByCpf("12345678901")).thenReturn(score);

		CustomerScoreResponseDTO result = customerScoreService.getCustomerScoreById(id);

		assertThat(result.id()).isEqualTo(1L);
		assertThat(result.name()).isEqualTo("Alice");
		assertThat(result.cpf()).isEqualTo("12345678901");
		assertThat(result.email()).isEqualTo("alice@test.com");
		assertThat(result.status()).isEqualTo("ACTIVE");
		assertThat(result.score()).isEqualTo(score);
		verify(customerService).findCustomerById(id);
		verify(scoreClientAdapter).getScoreByCpf("12345678901");
	}

	@Test
	@DisplayName("deve propagar CustomerNotFoundException quando cliente não existir")
	void devePropagarCustomerNotFoundExceptionQuandoClienteNaoExistir() {
		Long id = 99L;
		when(customerService.findCustomerById(id))
				.thenThrow(new CustomerNotFoundException("Customer not found with id: 99"));

		assertThatThrownBy(() -> customerScoreService.getCustomerScoreById(id))
				.isInstanceOf(CustomerNotFoundException.class)
				.hasMessageContaining("99");

		verify(scoreClientAdapter, never()).getScoreByCpf(org.mockito.ArgumentMatchers.any());
	}

	@Test
	@DisplayName("deve propagar exceção do score quando adapter falhar")
	void devePropagarExcecaoDoScoreQuandoAdapterFalhar() {
		Long id = 2L;
		Customer customer = buildCustomer(id, "Bruno", "98765432100", "bruno@test.com", StatusEnum.INACTIVE);
		when(customerService.findCustomerById(id)).thenReturn(customer);
		when(scoreClientAdapter.getScoreByCpf("98765432100"))
				.thenThrow(new ScoreServiceUnavailableException("Score service unavailable"));

		assertThatThrownBy(() -> customerScoreService.getCustomerScoreById(id))
				.isInstanceOf(ScoreServiceUnavailableException.class)
				.hasMessageContaining("unavailable");
	}

	@Test
	@DisplayName("deve consultar score com cpf nulo quando cliente vier sem cpf")
	void deveConsultarScoreComCpfNuloQuandoClienteVierSemCpf() {
		Long id = 3L;
		Customer customer = buildCustomer(id, "Carla", null, "carla@test.com", StatusEnum.ACTIVE);
		ScoreResponseDTO score = new ScoreResponseDTO(null, 500, "MEDIUM");

		when(customerService.findCustomerById(id)).thenReturn(customer);
		when(scoreClientAdapter.getScoreByCpf(null)).thenReturn(score);

		CustomerScoreResponseDTO result = customerScoreService.getCustomerScoreById(id);

		assertThat(result.cpf()).isNull();
		assertThat(result.score().score()).isEqualTo(500);
		verify(scoreClientAdapter).getScoreByCpf(null);
	}

	@Test
	@DisplayName("deve lançar exceção quando status do cliente for nulo")
	void deveLancarExcecaoQuandoStatusDoClienteForNulo() {
		Long id = 4L;
		Customer customer = buildCustomer(id, "Dani", "11122233344", "dani@test.com", null);
		ScoreResponseDTO score = new ScoreResponseDTO("11122233344", 600, "GOOD");

		when(customerService.findCustomerById(id)).thenReturn(customer);
		when(scoreClientAdapter.getScoreByCpf("11122233344")).thenReturn(score);

		assertThatThrownBy(() -> customerScoreService.getCustomerScoreById(id))
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("deve delegar busca quando id for nulo")
	void deveDelegarBuscaQuandoIdForNulo() {
		when(customerService.findCustomerById(null))
				.thenThrow(new CustomerNotFoundException("Customer not found with id: null"));

		assertThatThrownBy(() -> customerScoreService.getCustomerScoreById(null))
				.isInstanceOf(CustomerNotFoundException.class)
				.hasMessageContaining("null");

		verify(customerService).findCustomerById(null);
	}
}