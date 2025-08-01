package com.facturacion.model;

/**
 * Enumeración que representa los tipos de cliente en el sistema.
 */
public enum TipoCliente {
    OCASIONALES("Cliente Ocasional"),
    CORPORATIVOS("Cliente Corporativo");
    
    private final String descripcion;
    
    private TipoCliente(String descripcion) {
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
