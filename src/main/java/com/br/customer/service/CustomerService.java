package com.br.customer.service;

import com.br.customer.dtos.CustomerRequestDTO;
import com.br.customer.dtos.CustomerResponseDTO;
import com.br.customer.dtos.CustomerScoreResponseDTO;
import com.br.customer.exceptions.CustomerNotFoundException;
import com.br.customer.exceptions.DuplicateCpfException;
import com.br.customer.mapper.CustomerMapper;
import com.br.customer.model.Customer;
import com.br.customer.model.StatusEnum;
import com.br.customer.repository.CustomerJdbcRepository;
import com.br.customer.repository.CustomerRepository;
import com.br.score.client.ScoreClient;
import com.br.score.dto.ScoreResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CustomerJdbcRepository customerJdbcRepository;
    private final ScoreClient scoreClient;

    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper, CustomerJdbcRepository customerJdbcRepository, ScoreClient scoreClient) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.customerJdbcRepository = customerJdbcRepository;
        this.scoreClient = scoreClient;
    }

    public List<CustomerResponseDTO> getAllCustomers(StatusEnum status) {
        if (status != null)
            return customerJdbcRepository.findByStatus(status)
                    .stream()
                    .map(customerMapper::toResponse)
                    .toList();

        return customerRepository.findAll()
                .stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    public CustomerResponseDTO getCustomerById(Long id) {
        return customerMapper.toResponse(findCustomerById(id));
    }

    public CustomerScoreResponseDTO getCustomerScoreById(Long id) {
        Customer customer = findCustomerById(id);
        ScoreResponseDTO scoreResponseDTO = scoreClient.getScoreByCpf(customer.getCpf());

        return new CustomerScoreResponseDTO(customer.getId(), customer.getName(), customer.getCpf(), customer.getEmail(), customer.getStatus().name(), scoreResponseDTO);
    }

    public CustomerResponseDTO createCustomer(CustomerRequestDTO requestDTO) {
        validateUniqueCpf(requestDTO.cpf(), null);

        Customer customer = customerMapper.toEntity(requestDTO);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO requestDTO) {
        validateUniqueCpf(requestDTO.cpf(), id);

        Customer existingCustomer = findCustomerById(id);
        customerMapper.updateEntityFromRequest(existingCustomer, requestDTO);
        return customerMapper.toResponse(customerRepository.save(existingCustomer));
    }

    private void validateUniqueCpf(String cpf, Long excludeId) {
        customerRepository.findByCpf(cpf)
                .filter(c -> !c.getId().equals(excludeId))
                .ifPresent(c -> { throw new DuplicateCpfException(cpf); });
    }

    public void deleteCustomerById(Long id) {
        customerRepository.delete(findCustomerById(id));
    }

    public Customer findCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
    }

    public List<CustomerResponseDTO> searchByName(String name) {
        return customerRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(customerMapper::toResponse)
                .toList();
    }
}
