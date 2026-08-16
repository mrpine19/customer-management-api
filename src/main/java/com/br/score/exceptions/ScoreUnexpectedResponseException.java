package com.br.score.exceptions;

public class ScoreUnexpectedResponseException extends RuntimeException {
    public ScoreUnexpectedResponseException(String message) {
        super(message);
    }
}