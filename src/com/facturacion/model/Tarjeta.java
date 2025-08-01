package com.facturacion.model;

import java.util.regex.Pattern;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Enumeración que representa los tipos de tarjetas de crédito aceptadas en el sistema.
 * Incluye validación de números de tarjeta, fechas de vencimiento y códigos de seguridad.
 */
public enum Tarjeta {
    VISA("Visa", "^4[0-9]{12}(?:[0-9]{3})?$", "^[0-9]{3}$"),
    MASTERCARD("MasterCard", "^5[1-5][0-9]{14}$", "^[0-9]{3}$"),
    AMERICAN_EXPRESS("American Express", "^3[47][0-9]{13}$", "^[0-9]{4}$");
    
    private final String nombre;
    private final Pattern numeroPattern;
    private final Pattern codigoSeguridadPattern;
    
    private Tarjeta(String nombre, String numeroRegex, String codigoSeguridadRegex) {
        this.nombre = nombre;
        this.numeroPattern = Pattern.compile(numeroRegex);
        this.codigoSeguridadPattern = Pattern.compile(codigoSeguridadRegex);
    }
    
    public String getNombre() {
        return nombre;
    }
    
    /**
     * Valida el número de tarjeta según el tipo de tarjeta.
     * @param numero Número de tarjeta a validar
     * @return true si el número es válido, false en caso contrario
     */
    public boolean validarNumero(String numero) {
        if (numero == null) return false;
        
        // Eliminar espacios y guiones
        String numeroLimpio = numero.replaceAll("[\\s-]", "");
        
        // Validar formato
        if (!numeroPattern.matcher(numeroLimpio).matches()) {
            return false;
        }
        
        // Validar con algoritmo de Luhn
        return validarAlgoritmoLuhn(numeroLimpio);
    }
    
    /**
     * Valida el código de seguridad según el tipo de tarjeta.
     * @param codigo Código de seguridad a validar
     * @return true si el código es válido, false en caso contrario
     */
    public boolean validarCodigoSeguridad(String codigo) {
        if (codigo == null) return false;
        return codigoSeguridadPattern.matcher(codigo).matches();
    }
    
    /**
     * Valida la fecha de vencimiento de la tarjeta.
     * @param mesAño Fecha en formato "MM/YY" o "MM/YYYY"
     * @return true si la fecha es válida y no está vencida, false en caso contrario
     */
    public static boolean validarFechaVencimiento(String mesAño) {
        try {
            DateTimeFormatter formatter = mesAño.length() <= 5 ? 
                DateTimeFormatter.ofPattern("MM/yy") : 
                DateTimeFormatter.ofPattern("MM/yyyy");
                
            YearMonth vencimiento = YearMonth.parse(mesAño, formatter);
            YearMonth hoy = YearMonth.now();
            
            // La tarjeta es válida si el vencimiento es el mes actual o posterior
            return !vencimiento.isBefore(hoy);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Implementa el algoritmo de Luhn para validar números de tarjeta.
     * @param numero Número de tarjeta a validar
     * @return true si el número pasa la validación de Luhn, false en caso contrario
     */
    private boolean validarAlgoritmoLuhn(String numero) {
        int suma = 0;
        boolean doble = false;
        
        for (int i = numero.length() - 1; i >= 0; i--) {
            int digito = Character.getNumericValue(numero.charAt(i));
            
            if (doble) {
                digito *= 2;
                if (digito > 9) {
                    digito = (digito % 10) + 1;
                }
            }
            
            suma += digito;
            doble = !doble;
        }
        
        return (suma % 10) == 0;
    }
    
    @Override
    public String toString() {
        return nombre;
    }
}
