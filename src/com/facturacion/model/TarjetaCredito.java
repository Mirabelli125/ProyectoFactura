package com.facturacion.model;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * Clase que representa la información de una tarjeta de crédito.
 * Incluye validación de número de tarjeta, fecha de vencimiento y código de seguridad.
 */
public class TarjetaCredito {
    private final String numeroTarjeta;
    private final String nombreTitular;
    private final YearMonth fechaVencimiento;
    private final String codigoSeguridad;
    private final Tarjeta tipoTarjeta;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("MM/yy");

    /**
     * Constructor para crear una instancia de TarjetaCredito.
     * 
     * @param numeroTarjeta Número de la tarjeta (sin espacios ni guiones)
     * @param nombreTitular Nombre del titular como aparece en la tarjeta
     * @param fechaVencimiento Fecha de vencimiento en formato "MM/yy"
     * @param codigoSeguridad Código de seguridad (CVV/CVC)
     * @throws IllegalArgumentException Si algún dato de la tarjeta no es válido
     */
    public TarjetaCredito(String numeroTarjeta, String nombreTitular, 
                         String fechaVencimiento, String codigoSeguridad) {
        // Validar número de tarjeta
        String numeroLimpio = numeroTarjeta.replaceAll("[\\s-]", "");
        this.tipoTarjeta = determinarTipoTarjeta(numeroLimpio);
        
        if (this.tipoTarjeta == null) {
            throw new IllegalArgumentException("Número de tarjeta no válido o tipo de tarjeta no soportado");
        }
        
        if (!this.tipoTarjeta.validarNumero(numeroLimpio)) {
            throw new IllegalArgumentException("Número de tarjeta inválido para " + tipoTarjeta.getNombre());
        }
        
        // Validar nombre del titular
        if (nombreTitular == null || nombreTitular.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del titular no puede estar vacío");
        }
        
        // Validar fecha de vencimiento
        if (fechaVencimiento == null || !Tarjeta.validarFechaVencimiento(fechaVencimiento)) {
            throw new IllegalArgumentException("Fecha de vencimiento inválida o tarjeta vencida");
        }
        
        // Validar código de seguridad
        if (codigoSeguridad == null || !this.tipoTarjeta.validarCodigoSeguridad(codigoSeguridad)) {
            throw new IllegalArgumentException("Código de seguridad inválido para " + tipoTarjeta.getNombre());
        }
        
        this.numeroTarjeta = numeroLimpio;
        this.nombreTitular = nombreTitular.trim();
        this.fechaVencimiento = YearMonth.parse(fechaVencimiento, FORMATO_FECHA);
        this.codigoSeguridad = codigoSeguridad;
    }
    
    /**
     * Determina el tipo de tarjeta basado en el número.
     * @param numeroTarjeta Número de tarjeta (sin espacios ni guiones)
     * @return Tipo de tarjeta o null si no coincide con ningún tipo soportado
     */
    private Tarjeta determinarTipoTarjeta(String numeroTarjeta) {
        for (Tarjeta tarjeta : Tarjeta.values()) {
            if (tarjeta.validarNumero(numeroTarjeta)) {
                return tarjeta;
            }
        }
        return null;
    }
    
    // Getters
    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }
    
    public String getNumeroEnmascarado() {
        if (numeroTarjeta.length() < 4) {
            return "****";
        }
        return "**** **** **** " + numeroTarjeta.substring(numeroTarjeta.length() - 4);
    }

    public String getNombreTitular() {
        return nombreTitular;
    }

    public YearMonth getFechaVencimiento() {
        return fechaVencimiento;
    }
    
    public String getFechaVencimientoFormateada() {
        return fechaVencimiento.format(FORMATO_FECHA);
    }

    public String getCodigoSeguridad() {
        return codigoSeguridad;
    }

    public Tarjeta getTipoTarjeta() {
        return tipoTarjeta;
    }
    
    /**
     * Verifica si la tarjeta está vencida.
     * @return true si la tarjeta está vencida, false en caso contrario
     */
    public boolean estaVencida() {
        return YearMonth.now().isAfter(fechaVencimiento);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TarjetaCredito that = (TarjetaCredito) o;
        return numeroTarjeta.equals(that.numeroTarjeta) &&
               nombreTitular.equalsIgnoreCase(that.nombreTitular) &&
               fechaVencimiento.equals(that.fechaVencimiento) &&
               codigoSeguridad.equals(that.codigoSeguridad);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroTarjeta, nombreTitular.toLowerCase(), fechaVencimiento, codigoSeguridad);
    }

    @Override
    public String toString() {
        return String.format("Tarjeta %s\n" +
                           "Titular: %s\n" +
                           "Número: %s\n" +
                           "Vence: %s",
            tipoTarjeta.getNombre(),
            nombreTitular,
            getNumeroEnmascarado(),
            getFechaVencimientoFormateada());
    }
}
