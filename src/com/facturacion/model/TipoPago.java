package com.facturacion.model;

/**
 * Enumeración que representa los tipos de pago disponibles en el sistema.
 */
public enum TipoPago {
    CONTADO("Pago en Efectivo"),
    CREDITO("Tarjeta de Crédito");
    
    private final String descripcion;
    
    private TipoPago(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    @Override
    public String toString() {
        return descripcion;
    }
}
