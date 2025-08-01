package com.facturacion.model;

/**
 * Enumeración que representa los diferentes tipos de impuestos que se pueden aplicar a los productos.
 * Cada impuesto tiene un porcentaje específico.
 */
public enum Impuesto {
    EXENTO(0.0, "Exento"),
    OBT(1.0, "OTROS BIENES Y SERVICIOS (1%)"),
    MEDICINA(2.0, "MEDICINAS (2%)"),
    IVA(13.0, "IMPUESTO AL VALOR AGREGADO (13%)");
    
    private final double porcentaje;
    private final String descripcion;
    
    private Impuesto(double porcentaje, String descripcion) {
        this.porcentaje = porcentaje;
        this.descripcion = descripcion;
    }
    
    public double getPorcentaje() {
        return porcentaje;
    }
    
    public double calcularImpuesto(double monto) {
        return (monto * porcentaje) / 100.0;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    @Override
    public String toString() {
        return String.format("%s (%.2f%%)", descripcion, porcentaje);
    }
}
