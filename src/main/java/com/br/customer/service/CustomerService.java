package com.br.customer.service;

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
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
        return customerMapper.toResponse(customer);
    }

    public void deleteCustomerById(Long id) {
        if (!customerRepository.existsById(id))
            throw new CustomerNotFoundException("Customer not found with id: " + id);

        customerRepository.deleteById(id);
    }
}
