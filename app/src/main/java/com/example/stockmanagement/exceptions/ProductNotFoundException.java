package com.example.stockmanagement.exceptions;

public class ProductNotFoundException extends Throwable {
    public ProductNotFoundException(String s) {
        super("Product not found");
    }
}
