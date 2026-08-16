package com.br.score.adapter;

import com.br.score.client.ScoreClient;
import com.br.score.dto.ScoreResponseDTO;
import com.br.score.exceptions.ScoreNotFoundException;
import com.br.score.exceptions.ScoreServiceTimeoutException;
import com.br.score.exceptions.ScoreServiceUnavailableException;
import com.br.score.exceptions.ScoreUnexpectedResponseException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketTimeoutException;

@Component
public class ScoreClientAdapter {

    private final ScoreClient scoreClient;

    public ScoreClientAdapter(ScoreClient scoreClient) {
        this.scoreClient = scoreClient;
    }

    public ScoreResponseDTO getScoreByCpf(String cpf) {
        try {
            return scoreClient.getScoreByCpf(cpf);
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                throw new ScoreServiceTimeoutException("Timeout connecting to score service. Please try again later.");
            }
            throw new ScoreServiceUnavailableException("Error connecting to score service: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            throw new ScoreServiceUnavailableException("Score service error: " + e.getStatusCode());
        } catch (HttpClientErrorException.NotFound e) {
            throw new ScoreNotFoundException("Score not found for CPF: " + cpf);
        } catch (HttpClientErrorException e) {
            throw new ScoreUnexpectedResponseException("Unexpected response: " + e.getStatusCode());
        } catch (Exception e) {
            throw new ScoreUnexpectedResponseException("Unexpected error: " + e.getMessage());
        }
    }
}

