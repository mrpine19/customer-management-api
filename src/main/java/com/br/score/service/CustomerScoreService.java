package com.br.score.service;

import com.br.customer.dtos.CustomerScoreResponseDTO;
import com.br.customer.model.Customer;
import com.br.customer.service.CustomerService;
import com.br.score.adapter.ScoreClientAdapter;
import com.br.score.dto.ScoreResponseDTO;
import org.springframework.stereotype.Service;

@Service
public class CustomerScoreService {

    private final CustomerService customerService;
    private final ScoreClientAdapter scoreClientAdapter;

    public CustomerScoreService(CustomerService customerService, ScoreClientAdapter scoreClientAdapter) {
        this.customerService = customerService;
        this.scoreClientAdapter = scoreClientAdapter;
    }

    public CustomerScoreResponseDTO getCustomerScoreById(Long id) {
        Customer customer = customerService.findCustomerById(id);
        ScoreResponseDTO score = scoreClientAdapter.getScoreByCpf(customer.getCpf());

        return new CustomerScoreResponseDTO(
                customer.getId(),
                customer.getName(),
                customer.getCpf(),
                customer.getEmail(),
                customer.getStatus().name(),
                score
        );
    }
}