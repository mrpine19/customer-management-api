package com.br.score.exceptions;

public class ScoreNotFoundException extends RuntimeException {
    public ScoreNotFoundException(String message) {
        super(message);
    }
}