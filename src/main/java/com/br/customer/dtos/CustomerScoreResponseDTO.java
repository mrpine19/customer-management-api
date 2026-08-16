package com.br.customer.dtos;

import com.br.score.dto.ScoreResponseDTO;

public record CustomerScoreResponseDTO(
        Long id,
        String name,
        String cpf,
        String email,
        String status,
        ScoreResponseDTO score
) {
}
