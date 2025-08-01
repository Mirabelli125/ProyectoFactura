package com.facturacion.model;

/**
 * Enumeración que representa las monedas aceptadas en el sistema.
 */
public enum Moneda {
    COLONES("₡", "Colones Costarricenses", 1.0),
    DOLARES("$", "Dólares Estadounidenses", 0.0); // El tipo de cambio se actualizará según el día
    
    private final String simbolo;
    private final String nombre;
    private double tipoCambio; // Tipo de cambio respecto al colón
    
    private Moneda(String simbolo, String nombre, double tipoCambio) {
        this.simbolo = simbolo;
        this.nombre = nombre;
        this.tipoCambio = tipoCambio;
    }
    
    public String getSimbolo() {
        return simbolo;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public double getTipoCambio() {
        return tipoCambio;
    }
    
    public void setTipoCambio(double tipoCambio) {
        if (tipoCambio > 0) {
            this.tipoCambio = tipoCambio;
        }
    }
    
    /**
     * Convierte un monto de esta moneda a colones.
     * @param monto Monto a convertir
     * @return Monto equivalente en colones
     */
    public double aColones(double monto) {
        return monto * tipoCambio;
    }
    
    /**
     * Convierte un monto de colones a esta moneda.
     * @param monto Monto en colones
     * @return Monto equivalente en esta moneda
     */
    public double deColones(double monto) {
        if (tipoCambio == 0) return 0;
        return monto / tipoCambio;
    }
    
    @Override
    public String toString() {
        return String.format("%s %s", nombre, simbolo);
    }
}
