package com.br.customer.dtos;

import com.br.customer.model.StatusEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CustomerResponseDTO(
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
}
