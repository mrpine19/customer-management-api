package com.br.customer.service;

import com.br.customer.exceptions.CustomerNotFoundException;
import com.br.customer.model.Customer;
import com.br.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
    }

    public void deleteCustomerById(Long id) {
        if (!customerRepository.existsById(id))
            throw new CustomerNotFoundException("Customer not found with id: " + id);

        customerRepository.deleteById(id);
    }
}
