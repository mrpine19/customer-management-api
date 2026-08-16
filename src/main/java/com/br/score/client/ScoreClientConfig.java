package com.br.score.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.Builder;

import java.time.Duration;

@Configuration
public class ScoreClientConfig {

    @Bean
    public RestClient scoreRestClient(
            Builder builder,
            @Value("${score.api.base-url}") String baseUrl,
            @Value("${score.api.connect-timeout-ms:3000}") long connectTimeoutMs,
            @Value("${score.api.read-timeout-ms:5000}") long readTimeoutMs) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        return builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}

