package com.example.stockmanagement.exceptions;

public class DuplicateException extends RuntimeException {
    public DuplicateException(String name) {
        super("A product named '" + name + "' already exists.");
    }
}
