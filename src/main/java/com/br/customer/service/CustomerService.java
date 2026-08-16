package com.br.customer.service;

import com.br.customer.dtos.CustomerRequestDTO;
import com.br.customer.dtos.CustomerResponseDTO;
import com.br.customer.exceptions.CustomerNotFoundException;
import com.br.customer.mapper.CustomerMapper;
import com.br.customer.model.Customer;
import com.br.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    public CustomerResponseDTO getCustomerById(Long id) {
        return customerMapper.toResponse(findCustomerById(id));
    }

    public CustomerResponseDTO createCustomer(CustomerRequestDTO requestDTO) {
        Customer customer = customerMapper.toEntity(requestDTO);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO requestDTO) {
        Customer existingCustomer = findCustomerById(id);
        customerMapper.updateEntityFromRequest(existingCustomer, requestDTO);
        return customerMapper.toResponse(customerRepository.save(existingCustomer));
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

    public List<CustomerResponseDTO> searchByStatus(String status) {
        return customerRepository.findByStatusIgnoreCase(status)
                .stream()
                .map(customerMapper::toResponse)
                .toList();
    }
}
