package com.facturacion.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Clase que representa un pago en el sistema.
 * Maneja diferentes tipos de pago (efectivo, tarjeta) y monedas (colones, dólares).
 */
public class Pago {
    private static int contadorPago = 1;
    
    private final int numeroPago;
    private final LocalDateTime fechaHora;
    private final double monto;
    private final Moneda moneda;
    private final TipoPago tipoPago;
    private TarjetaCredito tarjetaCredito;
    private double tipoCambio;
    private double montoEnColones;

    /**
     * Constructor para pagos en efectivo.
     * 
     * @param monto Monto del pago
     * @param moneda Moneda del pago
     * @param tipoCambio Tipo de cambio a colones (si aplica)
     */
    public Pago(double monto, Moneda moneda, double tipoCambio) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        if (moneda == null) {
            throw new IllegalArgumentException("La moneda no puede ser nula");
        }
        
        this.numeroPago = contadorPago++;
        this.fechaHora = LocalDateTime.now();
        this.monto = monto;
        this.moneda = moneda;
        this.tipoPago = TipoPago.CONTADO;
        this.tipoCambio = (moneda == Moneda.DOLARES) ? tipoCambio : 1.0;
        this.montoEnColones = (moneda == Moneda.DOLARES) ? monto * tipoCambio : monto;
    }
    
    /**
     * Constructor para pagos con tarjeta de crédito.
     * 
     * @param monto Monto del pago
     * @param tarjetaCredito Información de la tarjeta de crédito
     */
    public Pago(double monto, TarjetaCredito tarjetaCredito) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        if (tarjetaCredito == null) {
            throw new IllegalArgumentException("La información de la tarjeta no puede ser nula");
        }
        
        this.numeroPago = contadorPago++;
        this.fechaHora = LocalDateTime.now();
        this.monto = monto;
        this.moneda = Moneda.COLONES; // Los pagos con tarjeta siempre son en colones
        this.tipoPago = TipoPago.CREDITO;
        this.tarjetaCredito = tarjetaCredito;
        this.tipoCambio = 1.0;
        this.montoEnColones = monto;
    }

    // Getters
    public int getNumeroPago() {
        return numeroPago;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public double getMonto() {
        return monto;
    }

    public Moneda getMoneda() {
        return moneda;
    }

    public TipoPago getTipoPago() {
        return tipoPago;
    }

    public TarjetaCredito getTarjetaCredito() {
        return tarjetaCredito;
    }
    
    public double getTipoCambio() {
        return tipoCambio;
    }
    
    public double getMontoEnColones() {
        return montoEnColones;
    }
    
    /**
     * Obtiene el monto en la moneda especificada.
     * @param monedaDestino Moneda a la que se desea convertir
     * @return Monto convertido a la moneda destino
     */
    public double getMontoEnMoneda(Moneda monedaDestino) {
        if (monedaDestino == moneda) {
            return monto;
        }
        if (monedaDestino == Moneda.COLONES) {
            return montoEnColones;
        } else {
            // Convertir a dólares
            return montoEnColones / tipoCambio;
        }
    }
    
    /**
     * Verifica si el pago fue realizado en efectivo.
     * @return true si es pago en efectivo, false si es con tarjeta
     */
    public boolean esEfectivo() {
        return tipoPago == TipoPago.CONTADO;
    }
    
    /**
     * Verifica si el pago fue realizado con tarjeta de crédito.
     * @return true si es pago con tarjeta, false si es en efectivo
     */
    public boolean esTarjeta() {
        return tipoPago == TipoPago.CREDITO;
    }
    
    /**
     * Verifica si el pago fue realizado en dólares.
     * @return true si es en dólares, false si es en colones
     */
    public boolean esEnDolares() {
        return moneda == Moneda.DOLARES;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pago pago = (Pago) o;
        return numeroPago == pago.numeroPago;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroPago);
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String fechaStr = fechaHora.format(formatter);
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Pago #%d - %s\n", numeroPago, fechaStr));
        sb.append(String.format("Tipo: %s\n", tipoPago.getDescripcion()));
        sb.append(String.format("Monto: %.2f %s", monto, moneda.getSimbolo()));
        
        if (moneda == Moneda.DOLARES) {
            sb.append(String.format(" (Tipo de cambio: %.2f, Total: %.2f ₡)\n", 
                tipoCambio, montoEnColones));
        } else {
            sb.append("\n");
        }
        
        if (tipoPago == TipoPago.CREDITO && tarjetaCredito != null) {
            sb.append(tarjetaCredito.toString());
        }
        
        return sb.toString();
    }
}
