package com.bts.banque.exceptions;

public class CompteDejaExistantException extends RuntimeException {

    public CompteDejaExistantException(String message) {
        super(message);
    }
}
