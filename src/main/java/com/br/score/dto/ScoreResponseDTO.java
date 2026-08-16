package com.br.score.dto;

public record ScoreResponseDTO(
        String cpf,
        Integer score,
        String classification
) {
}
