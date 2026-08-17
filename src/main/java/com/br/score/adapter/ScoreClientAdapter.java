package com.br.score.adapter;

import com.br.score.client.ScoreClient;
import com.br.score.dto.ScoreResponseDTO;
import com.br.score.exceptions.ScoreNotFoundException;
import com.br.score.exceptions.ScoreServiceTimeoutException;
import com.br.score.exceptions.ScoreServiceUnavailableException;
import com.br.score.exceptions.ScoreUnexpectedResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;

@Slf4j
@Component
public class ScoreClientAdapter {

    private final ScoreClient scoreClient;

    public ScoreClientAdapter(ScoreClient scoreClient) {
        this.scoreClient = scoreClient;
    }

    public ScoreResponseDTO getScoreByCpf(String cpf) {
        log.info("Calling external score service for CPF: {}", cpf);
        long startTime = System.currentTimeMillis();
        try {
            ScoreResponseDTO response = scoreClient.getScoreByCpf(cpf);
            long duration = System.currentTimeMillis() - startTime;
            log.info("Successfully received score for CPF: {} in {}ms.", cpf, duration);
            return response;
        } catch (ResourceAccessException e) {
            long duration = System.currentTimeMillis() - startTime;
            if (e.getCause() instanceof SocketTimeoutException || e.getCause() instanceof HttpTimeoutException) {
                log.error("Timeout calling score service for CPF: {} after {}ms.", cpf, duration, e);
                throw new ScoreServiceTimeoutException("Timeout connecting to score service. Please try again later.");
            }
            log.error("Resource access error calling score service for CPF: {} after {}ms.", cpf, duration, e);
            throw new ScoreServiceUnavailableException("Error connecting to score service: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Score service returned server error (HTTP {}) for CPF: {} after {}ms.", e.getStatusCode(), cpf, duration, e);
            throw new ScoreServiceUnavailableException("Score service error: " + e.getStatusCode());
        } catch (HttpClientErrorException.NotFound e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("Score service returned not found (HTTP 404) for CPF: {} after {}ms.", cpf, duration);
            throw new ScoreNotFoundException("Score not found for CPF: " + cpf);
        } catch (HttpClientErrorException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Score service returned client error (HTTP {}) for CPF: {} after {}ms.", e.getStatusCode(), cpf, duration, e);
            throw new ScoreUnexpectedResponseException("Unexpected response: " + e.getStatusCode());
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("An unexpected error occurred while calling score service for CPF: {} after {}ms.", cpf, duration, e);
            throw new ScoreUnexpectedResponseException("Unexpected error: " + e.getMessage());
        }
    }
}
