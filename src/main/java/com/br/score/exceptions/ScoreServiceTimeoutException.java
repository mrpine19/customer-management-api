package com.br.score.exceptions;

public class ScoreServiceTimeoutException extends RuntimeException {
    public ScoreServiceTimeoutException(String message) {
        super(message);
    }
}