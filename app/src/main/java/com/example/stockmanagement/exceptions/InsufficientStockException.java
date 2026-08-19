package com.example.stockmanagement.exceptions;

public class InsufficientStockException extends Throwable {
    public InsufficientStockException(String message) {
        super(message);
    }
    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
}}
