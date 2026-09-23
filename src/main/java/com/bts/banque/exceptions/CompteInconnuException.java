package com.bts.banque.exceptions;

public class CompteInconnuException extends RuntimeException {

    public CompteInconnuException(String message) {
        super(message);
    }
}
