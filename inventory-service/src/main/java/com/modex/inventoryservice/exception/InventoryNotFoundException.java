package com.modex.inventoryservice.exception;

public class InventoryNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InventoryNotFoundException(String productId, String size) {
        super("No existe inventario para productId: " + productId + " talla: " + size);
    }
}