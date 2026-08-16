package com.br.score.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.Builder;

@Configuration
public class ScoreClientConfig {

    @Bean
    public RestClient scoreRestClient(
            Builder builder,
            @Value("${score.api.base-url}") String baseUrl) {
        return builder
                .baseUrl(baseUrl)
                .build();
    }
}

