package com.br.score.exceptions;

public class ScoreServiceUnavailableException extends RuntimeException {
    public ScoreServiceUnavailableException(String message) {
        super(message);
    }
}