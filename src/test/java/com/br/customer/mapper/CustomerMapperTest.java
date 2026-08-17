package com.br.customer.mapper;

import com.br.customer.dtos.CustomerRequestDTO;
import com.br.customer.dtos.CustomerResponseDTO;
import com.br.customer.model.Customer;
import com.br.customer.model.StatusEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class CustomerMapperTest {

	@InjectMocks
	private CustomerMapper customerMapper;

    private Customer buildCustomer(
            Long id,
            String name,
            String cpf,
            String email,
            String phone,
            StatusEnum status,
            LocalDate birthDate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        customer.setCpf(cpf);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setStatus(status);
        customer.setBirthDate(birthDate);
        customer.setCreatedAt(createdAt);
        customer.setUpdatedAt(updatedAt);
        return customer;
    }

    private CustomerRequestDTO buildRequest(
            String name,
            String cpf,
            String email,
            String phone,
            StatusEnum status,
            LocalDate birthDate
    ) {
        return new CustomerRequestDTO(name, cpf, email, phone, status, birthDate);
    }

	@Test
	@DisplayName("deve mapear Customer para CustomerResponseDTO com todos os campos")
	void deveMapearCustomerParaResponseComSucesso() {
		Customer customer = buildCustomer(
				1L,
				"Alice",
				"12345678901",
				"alice@test.com",
				"11999990000",
				StatusEnum.ACTIVE,
				LocalDate.of(1990, 1, 15),
				LocalDateTime.of(2024, 1, 1, 10, 0),
				LocalDateTime.of(2024, 1, 1, 11, 0)
		);

		CustomerResponseDTO result = customerMapper.toResponse(customer);

		assertThat(result.id()).isEqualTo(1L);
		assertThat(result.name()).isEqualTo("Alice");
		assertThat(result.cpf()).isEqualTo("12345678901");
		assertThat(result.email()).isEqualTo("alice@test.com");
		assertThat(result.phone()).isEqualTo("11999990000");
		assertThat(result.status()).isEqualTo(StatusEnum.ACTIVE);
		assertThat(result.birthDate()).isEqualTo(LocalDate.of(1990, 1, 15));
		assertThat(result.createdAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
		assertThat(result.updatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 11, 0));
	}

	@Test
	@DisplayName("deve mapear CustomerRequestDTO para Customer com sucesso")
	void deveMapearRequestParaEntityComSucesso() {
		CustomerRequestDTO requestDTO = buildRequest(
				"Bruno",
				"98765432100",
				"bruno@test.com",
				"21999990000",
				StatusEnum.INACTIVE,
				LocalDate.of(1988, 5, 20)
		);

		Customer result = customerMapper.toEntity(requestDTO);

		assertThat(result.getId()).isNull();
		assertThat(result.getName()).isEqualTo("Bruno");
		assertThat(result.getCpf()).isEqualTo("98765432100");
		assertThat(result.getEmail()).isEqualTo("bruno@test.com");
		assertThat(result.getPhone()).isEqualTo("21999990000");
		assertThat(result.getStatus()).isEqualTo(StatusEnum.INACTIVE);
		assertThat(result.getBirthDate()).isEqualTo(LocalDate.of(1988, 5, 20));
	}

	@Test
	@DisplayName("deve atualizar entidade com dados do request")
	void deveAtualizarEntityComDadosDoRequest() {
		Customer customer = buildCustomer(
				10L,
				"Nome Antigo",
				"00000000000",
				"old@test.com",
				"11911110000",
				StatusEnum.ACTIVE,
				LocalDate.of(1991, 1, 1),
				LocalDateTime.of(2024, 2, 1, 8, 0),
				LocalDateTime.of(2024, 2, 1, 9, 0)
		);
		CustomerRequestDTO requestDTO = buildRequest(
				"Nome Novo",
				"99999999999",
				"new@test.com",
				"11922220000",
				StatusEnum.SUSPENDED,
				LocalDate.of(1993, 3, 3)
		);

		customerMapper.updateEntityFromRequest(customer, requestDTO);

		assertThat(customer.getId()).isEqualTo(10L);
		assertThat(customer.getName()).isEqualTo("Nome Novo");
		assertThat(customer.getCpf()).isEqualTo("99999999999");
		assertThat(customer.getEmail()).isEqualTo("new@test.com");
		assertThat(customer.getPhone()).isEqualTo("11922220000");
		assertThat(customer.getStatus()).isEqualTo(StatusEnum.SUSPENDED);
		assertThat(customer.getBirthDate()).isEqualTo(LocalDate.of(1993, 3, 3));
	}

	@Test
	@DisplayName("deve mapear campos nulos e vazios sem falhar")
	void deveMapearCamposNulosEVaziosSemFalhar() {
		CustomerRequestDTO requestDTO = buildRequest("", "", null, "", null, null);

		Customer result = customerMapper.toEntity(requestDTO);

		assertThat(result.getName()).isEmpty();
		assertThat(result.getCpf()).isEmpty();
		assertThat(result.getEmail()).isNull();
		assertThat(result.getPhone()).isEmpty();
		assertThat(result.getStatus()).isNull();
		assertThat(result.getBirthDate()).isNull();
	}

	@Test
	@DisplayName("deve lançar NullPointerException quando toResponse receber customer nulo")
	void deveLancarExcecaoQuandoToResponseReceberCustomerNulo() {
		assertThatThrownBy(() -> customerMapper.toResponse(null))
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("deve lançar NullPointerException quando toEntity receber request nulo")
	void deveLancarExcecaoQuandoToEntityReceberRequestNulo() {
		assertThatThrownBy(() -> customerMapper.toEntity(null))
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("deve lançar NullPointerException quando updateEntityFromRequest receber customer nulo")
	void deveLancarExcecaoQuandoUpdateReceberCustomerNulo() {
		CustomerRequestDTO requestDTO = buildRequest(
				"Alice",
				"12345678901",
				"alice@test.com",
				"11999990000",
				StatusEnum.ACTIVE,
				LocalDate.of(1990, 1, 15)
		);

		assertThatThrownBy(() -> customerMapper.updateEntityFromRequest(null, requestDTO))
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	@DisplayName("deve lançar NullPointerException quando updateEntityFromRequest receber request nulo")
	void deveLancarExcecaoQuandoUpdateReceberRequestNulo() {
		Customer customer = buildCustomer(
				1L,
				"Alice",
				"12345678901",
				"alice@test.com",
				"11999990000",
				StatusEnum.ACTIVE,
				LocalDate.of(1990, 1, 15),
				LocalDateTime.of(2024, 1, 1, 10, 0),
				LocalDateTime.of(2024, 1, 1, 11, 0)
		);

		assertThatThrownBy(() -> customerMapper.updateEntityFromRequest(customer, null))
				.isInstanceOf(NullPointerException.class);
	}
}