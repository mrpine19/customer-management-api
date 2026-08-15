package com.br.customer.mapper;

import com.br.customer.dtos.CustomerRequestDTO;
import com.br.customer.dtos.CustomerResponseDTO;
import com.br.customer.model.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerResponseDTO toResponse(Customer customer) {
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

    public Customer toEntity(CustomerRequestDTO requestDTO) {
        Customer customer = new Customer();
        customer.setName(requestDTO.name());
        customer.setCpf(requestDTO.cpf());
        customer.setEmail(requestDTO.email());
        customer.setPhone(requestDTO.phone());
        customer.setStatus(requestDTO.status());
        customer.setBirthDate(requestDTO.birthDate());
        return customer;
    }

    public void updateEntityFromRequest(Customer customer, CustomerRequestDTO requestDTO) {
        customer.setName(requestDTO.name());
        customer.setCpf(requestDTO.cpf());
        customer.setEmail(requestDTO.email());
        customer.setPhone(requestDTO.phone());
        customer.setStatus(requestDTO.status());
        customer.setBirthDate(requestDTO.birthDate());
    }
}
