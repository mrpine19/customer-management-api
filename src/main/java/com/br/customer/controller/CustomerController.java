package com.br.customer.controller;

import com.br.customer.dtos.CustomerRequestDTO;
import com.br.customer.dtos.CustomerResponseDTO;
import com.br.customer.dtos.CustomerScoreResponseDTO;
import com.br.customer.model.StatusEnum;
import com.br.score.service.CustomerScoreService;
import com.br.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerScoreService customerScoreService;

    public CustomerController(CustomerService customerService, CustomerScoreService customerScoreService) {
        this.customerService = customerService;
        this.customerScoreService = customerScoreService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CustomerResponseDTO> getAllCustomers(@RequestParam(required = false) StatusEnum status) {
        log.info("Starting customer retrieval. Status filter: {}", status);
        return customerService.getAllCustomers(status);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CustomerResponseDTO getCustomerById(@PathVariable Long id) {
        log.info("Starting retrieval for customer ID: {}", id);
        return customerService.getCustomerById(id);
    }

    @GetMapping("/{id}/score")
    @ResponseStatus(HttpStatus.OK)
    public CustomerScoreResponseDTO getCustomerScoreById(@PathVariable Long id) {
        log.info("Starting score retrieval for customer ID: {}", id);
        return customerScoreService.getCustomerScoreById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponseDTO createCustomer(@Valid @RequestBody CustomerRequestDTO requestDTO) {
        log.info("Starting creation for new customer.");
        log.debug("Request payload for new customer: {}", requestDTO);
        return customerService.createCustomer(requestDTO);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CustomerResponseDTO updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequestDTO requestDTO) {
        log.info("Starting update for customer ID: {}", id);
        log.debug("Request payload for customer ID {}: {}", id, requestDTO);
        return customerService.updateCustomer(id, requestDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomerById(@PathVariable Long id) {
        log.info("Starting deletion for customer ID: {}", id);
        customerService.deleteCustomerById(id);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<CustomerResponseDTO> searchByName(@RequestParam String name) {
        log.info("Starting customer search by name: '{}'", name);
        return customerService.searchByName(name);
    }
}
