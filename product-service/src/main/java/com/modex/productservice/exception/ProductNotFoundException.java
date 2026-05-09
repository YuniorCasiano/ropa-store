package com.modex.productservice.exception;

public class ProductNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProductNotFoundException(String id) {
        super("No existe producto con el id: " + id);
    }

    public ProductNotFoundException(String field, String value) {
        super("No existe producto con " + field + ": " + value);
    }
}