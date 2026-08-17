package com.br.customer.repository;

import com.br.customer.model.Customer;
import com.br.customer.model.StatusEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerJdbcRepositoryTest {

	@Mock
	private JdbcTemplate jdbcTemplate;

	@InjectMocks
	private CustomerJdbcRepository customerJdbcRepository;

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
	@DisplayName("deve retornar clientes por status quando consulta executa com sucesso")
	void deveRetornarClientesPorStatusQuandoConsultaComSucesso() {
		Customer customer = buildCustomer(1L, "Alice", "12345678901", StatusEnum.ACTIVE);
		when(jdbcTemplate.query(anyString(), ArgumentMatchers.<RowMapper<Customer>>any(), eq("ACTIVE")))
				.thenReturn(List.of(customer));

		List<Customer> result = customerJdbcRepository.findByStatus(StatusEnum.ACTIVE);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getStatus()).isEqualTo(StatusEnum.ACTIVE);
		verify(jdbcTemplate, times(1)).query(anyString(), ArgumentMatchers.<RowMapper<Customer>>any(), eq("ACTIVE"));
	}

	@Test
	@DisplayName("deve retornar lista vazia quando não houver clientes para o status informado")
	void deveRetornarListaVaziaQuandoNaoHouverClientesParaStatus() {
		when(jdbcTemplate.query(anyString(), ArgumentMatchers.<RowMapper<Customer>>any(), eq("INACTIVE")))
				.thenReturn(Collections.emptyList());

		List<Customer> result = customerJdbcRepository.findByStatus(StatusEnum.INACTIVE);

		assertThat(result).isEmpty();
		verify(jdbcTemplate, times(1)).query(anyString(), ArgumentMatchers.<RowMapper<Customer>>any(), eq("INACTIVE"));
	}

	@Test
	@DisplayName("deve propagar exceção quando JdbcTemplate falhar")
	void devePropagarExcecaoQuandoJdbcTemplateFalhar() {
		when(jdbcTemplate.query(anyString(), ArgumentMatchers.<RowMapper<Customer>>any(), eq("SUSPENDED")))
				.thenThrow(new RuntimeException("erro de banco"));

		assertThatThrownBy(() -> customerJdbcRepository.findByStatus(StatusEnum.SUSPENDED))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("erro de banco");
	}

	@Test
	@DisplayName("deve lançar NullPointerException quando status for nulo")
	void deveLancarExcecaoQuandoStatusForNulo() {
		assertThatThrownBy(() -> customerJdbcRepository.findByStatus(null))
				.isInstanceOf(NullPointerException.class);

		verify(jdbcTemplate, never()).query(anyString(), ArgumentMatchers.<RowMapper<Customer>>any(), any());
	}
}