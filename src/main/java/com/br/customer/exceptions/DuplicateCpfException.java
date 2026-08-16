package com.br.customer.exceptions;

public class DuplicateCpfException extends RuntimeException {
    public DuplicateCpfException(String cpf) {
        super("CPF already registered: " + cpf);
    }
}