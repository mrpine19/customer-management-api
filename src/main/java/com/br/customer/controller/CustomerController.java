package com.br.customer.controller;

import com.br.customer.dtos.CustomerRequestDTO;
import com.br.customer.dtos.CustomerResponseDTO;
import com.br.customer.model.Customer;
import com.br.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{id}")
    public CustomerResponseDTO getCustomerById(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }

    @PostMapping
    public CustomerResponseDTO createCustomer(@Valid @RequestBody CustomerRequestDTO requestDTO) {
        return customerService.createCustomer(requestDTO);
    }

    @PutMapping("/{id}")
    public CustomerResponseDTO updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequestDTO requestDTO) {
        return customerService.updateCustomer(id, requestDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteCustomerById(@PathVariable Long id) {
        customerService.deleteCustomerById(id);
    }
}
