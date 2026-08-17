package com.br.customer.service;

import com.br.customer.dtos.CustomerRequestDTO;
import com.br.customer.dtos.CustomerResponseDTO;
import com.br.customer.exceptions.CustomerNotFoundException;
import com.br.customer.exceptions.DuplicateCpfException;
import com.br.customer.mapper.CustomerMapper;
import com.br.customer.model.Customer;
import com.br.customer.model.StatusEnum;
import com.br.customer.repository.CustomerJdbcRepository;
import com.br.customer.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private CustomerMapper customerMapper;

	@Mock
	private CustomerJdbcRepository customerJdbcRepository;

	@InjectMocks
	private CustomerService customerService;

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

    private CustomerResponseDTO buildResponseDTO(Customer customer) {
        return new CustomerResponseDTO(
                customer.getId(),
                customer.getName(),
                customer.getCpf(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getStatus(),
                customer.getBirthDate(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

	@Test
	@DisplayName("deve retornar clientes filtrados por status quando status é informado")
	void deveRetornarClientesFiltradosPorStatus() {
		Customer customer = buildCustomer(1L, "Alice", "12345678901", StatusEnum.ACTIVE);
		CustomerResponseDTO response = buildResponseDTO(customer);
		when(customerJdbcRepository.findByStatus(StatusEnum.ACTIVE)).thenReturn(List.of(customer));
		when(customerMapper.toResponse(customer)).thenReturn(response);

		List<CustomerResponseDTO> result = customerService.getAllCustomers(StatusEnum.ACTIVE);

		assertThat(result).hasSize(1).containsExactly(response);
		verify(customerJdbcRepository, times(1)).findByStatus(StatusEnum.ACTIVE);
		verify(customerRepository, never()).findAll();
	}

	@Test
	@DisplayName("deve retornar todos os clientes quando status for nulo")
	void deveRetornarTodosClientesQuandoStatusNulo() {
		Customer customer = buildCustomer(2L, "Bruno", "98765432100", StatusEnum.INACTIVE);
		CustomerResponseDTO response = buildResponseDTO(customer);
		when(customerRepository.findAll()).thenReturn(List.of(customer));
		when(customerMapper.toResponse(customer)).thenReturn(response);

		List<CustomerResponseDTO> result = customerService.getAllCustomers(null);

		assertThat(result).hasSize(1).containsExactly(response);
		verify(customerRepository, times(1)).findAll();
		verify(customerJdbcRepository, never()).findByStatus(any());
	}

	@Test
	@DisplayName("deve retornar lista vazia quando não há clientes")
	void deveRetornarListaVaziaQuandoNaoHaClientes() {
		when(customerRepository.findAll()).thenReturn(Collections.emptyList());

		List<CustomerResponseDTO> result = customerService.getAllCustomers(null);

		assertThat(result).isEmpty();
		verify(customerMapper, never()).toResponse(any());
	}

	@Test
	@DisplayName("deve retornar cliente por id quando existir")
	void deveRetornarClientePorIdQuandoExistir() {
		Customer customer = buildCustomer(10L, "Carlos", "11122233344", StatusEnum.ACTIVE);
		CustomerResponseDTO response = buildResponseDTO(customer);
		when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
		when(customerMapper.toResponse(customer)).thenReturn(response);

		CustomerResponseDTO result = customerService.getCustomerById(10L);

		assertThat(result).isEqualTo(response);
		verify(customerRepository, times(1)).findById(10L);
	}

	@Test
	@DisplayName("deve lançar CustomerNotFoundException quando id não existir")
	void deveLancarExcecaoQuandoIdNaoExistir() {
		when(customerRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> customerService.getCustomerById(99L))
				.isInstanceOf(CustomerNotFoundException.class)
				.hasMessageContaining("99");
	}

	@Test
	@DisplayName("deve criar cliente quando CPF é único")
	void deveCriarClienteQuandoCpfUnico() {
		CustomerRequestDTO request = buildRequestDTO("Debora", "22233344455", StatusEnum.ACTIVE);
		Customer entity = buildCustomer(null, "Debora", "22233344455", StatusEnum.ACTIVE);
		Customer saved = buildCustomer(3L, "Debora", "22233344455", StatusEnum.ACTIVE);
		CustomerResponseDTO response = buildResponseDTO(saved);

		when(customerRepository.findByCpf("22233344455")).thenReturn(Optional.empty());
		when(customerMapper.toEntity(request)).thenReturn(entity);
		when(customerRepository.save(entity)).thenReturn(saved);
		when(customerMapper.toResponse(saved)).thenReturn(response);

		CustomerResponseDTO result = customerService.createCustomer(request);

		assertThat(result).isEqualTo(response);
		verify(customerRepository, times(1)).save(entity);
	}

	@Test
	@DisplayName("deve lançar DuplicateCpfException ao criar cliente com CPF duplicado")
	void deveLancarExcecaoAoCriarComCpfDuplicado() {
		CustomerRequestDTO request = buildRequestDTO("Edu", "33344455566", StatusEnum.ACTIVE);
		Customer existing = buildCustomer(7L, "Outro", "33344455566", StatusEnum.INACTIVE);
		when(customerRepository.findByCpf("33344455566")).thenReturn(Optional.of(existing));

		assertThatThrownBy(() -> customerService.createCustomer(request))
				.isInstanceOf(DuplicateCpfException.class)
				.hasMessageContaining("33344455566");

		verify(customerMapper, never()).toEntity(any());
		verify(customerRepository, never()).save(any());
	}

	@Test
	@DisplayName("deve atualizar cliente quando CPF pertence ao próprio cliente")
	void deveAtualizarClienteQuandoCpfPertenceAoProprioCliente() {
		Long id = 8L;
		CustomerRequestDTO request = buildRequestDTO("Fernanda", "44455566677", StatusEnum.SUSPENDED);
		Customer existing = buildCustomer(id, "Fernanda", "44455566677", StatusEnum.ACTIVE);
		CustomerResponseDTO response = buildResponseDTO(existing);

		when(customerRepository.findByCpf("44455566677")).thenReturn(Optional.of(existing));
		when(customerRepository.findById(id)).thenReturn(Optional.of(existing));
		when(customerRepository.save(existing)).thenReturn(existing);
		when(customerMapper.toResponse(existing)).thenReturn(response);

		CustomerResponseDTO result = customerService.updateCustomer(id, request);

		assertThat(result).isEqualTo(response);
		verify(customerMapper, times(1)).updateEntityFromRequest(existing, request);
		verify(customerRepository, times(1)).save(existing);
	}

	@Test
	@DisplayName("deve lançar DuplicateCpfException ao atualizar com CPF de outro cliente")
	void deveLancarExcecaoAoAtualizarComCpfDeOutroCliente() {
		Long id = 15L;
		CustomerRequestDTO request = buildRequestDTO("Gabi", "55566677788", StatusEnum.ACTIVE);
		Customer other = buildCustomer(99L, "Outro", "55566677788", StatusEnum.ACTIVE);
		when(customerRepository.findByCpf("55566677788")).thenReturn(Optional.of(other));

		assertThatThrownBy(() -> customerService.updateCustomer(id, request))
				.isInstanceOf(DuplicateCpfException.class)
				.hasMessageContaining("55566677788");

		verify(customerRepository, never()).findById(any());
		verify(customerRepository, never()).save(any());
	}

	@Test
	@DisplayName("deve lançar CustomerNotFoundException ao atualizar cliente inexistente")
	void deveLancarExcecaoAoAtualizarClienteInexistente() {
		Long id = 50L;
		CustomerRequestDTO request = buildRequestDTO("Igor", "12312312312", StatusEnum.ACTIVE);
		when(customerRepository.findByCpf("12312312312")).thenReturn(Optional.empty());
		when(customerRepository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> customerService.updateCustomer(id, request))
				.isInstanceOf(CustomerNotFoundException.class)
				.hasMessageContaining("50");
	}

	@Test
	@DisplayName("deve excluir cliente quando existir")
	void deveExcluirClienteQuandoExistir() {
		Customer customer = buildCustomer(20L, "Helena", "66677788899", StatusEnum.ACTIVE);
		when(customerRepository.findById(20L)).thenReturn(Optional.of(customer));

		customerService.deleteCustomerById(20L);

		verify(customerRepository, times(1)).delete(customer);
	}

	@Test
	@DisplayName("deve lançar CustomerNotFoundException ao excluir cliente inexistente")
	void deveLancarExcecaoAoExcluirClienteInexistente() {
		when(customerRepository.findById(0L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> customerService.deleteCustomerById(0L))
				.isInstanceOf(CustomerNotFoundException.class)
				.hasMessageContaining("0");

		verify(customerRepository, never()).delete(any());
	}

	@Test
	@DisplayName("deve propagar exceção quando repositório falhar ao excluir")
	void devePropagarExcecaoQuandoRepositorioFalharAoExcluir() {
		Customer customer = buildCustomer(21L, "Iris", "77788899900", StatusEnum.ACTIVE);
		when(customerRepository.findById(21L)).thenReturn(Optional.of(customer));
		doThrow(new RuntimeException("falha ao deletar")).when(customerRepository).delete(customer);

		assertThatThrownBy(() -> customerService.deleteCustomerById(21L))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("falha ao deletar");
	}

	@Test
	@DisplayName("deve buscar clientes por nome")
	void deveBuscarClientesPorNome() {
		Customer customer = buildCustomer(30L, "Ana Paula", "10101010101", StatusEnum.ACTIVE);
		CustomerResponseDTO response = buildResponseDTO(customer);
		when(customerRepository.findByNameContainingIgnoreCase("Ana")).thenReturn(List.of(customer));
		when(customerMapper.toResponse(customer)).thenReturn(response);

		List<CustomerResponseDTO> result = customerService.searchByName("Ana");

		assertThat(result).hasSize(1).containsExactly(response);
		verify(customerRepository, times(1)).findByNameContainingIgnoreCase("Ana");
	}

	@Test
	@DisplayName("deve retornar lista vazia ao buscar por nome vazio")
	void deveRetornarListaVaziaAoBuscarPorNomeVazio() {
		when(customerRepository.findByNameContainingIgnoreCase("")).thenReturn(Collections.emptyList());

		List<CustomerResponseDTO> result = customerService.searchByName("");

		assertThat(result).isEmpty();
		verify(customerMapper, never()).toResponse(any());
	}

	private CustomerRequestDTO buildRequestDTO(String name, String cpf, StatusEnum status) {
		return new CustomerRequestDTO(
				name,
				cpf,
				"email@test.com",
				"11999990000",
				status,
				LocalDate.of(1990, 1, 15)
		);
	}
}