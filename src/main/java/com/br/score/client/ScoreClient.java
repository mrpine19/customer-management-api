package com.br.score.client;

import com.br.score.dto.ScoreResponseDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ScoreClient {

    private final RestClient restClient;

    public ScoreClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public ScoreResponseDTO getScoreByCpf(String cpf) {
        return restClient.get()
                .uri("/scores/{cpf}", cpf)
                .retrieve()
                .body(ScoreResponseDTO.class);
    }
}
