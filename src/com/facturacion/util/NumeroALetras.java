package com.facturacion.util;

/**
 * Utilidad para convertir números a su representación en letras en español.
 * Soporta números enteros y decimales (hasta céntimos).
 */
public class NumeroALetras {
    
    private static final String[] UNIDADES = {
        "", "UN ", "DOS ", "TRES ", "CUATRO ", "CINCO ", "SEIS ", "SIETE ", "OCHO ", "NUEVE "
    };
    
    private static final String[] DECENAS = {
        "DIEZ ", "ONCE ", "DOCE ", "TRECE ", "CATORCE ", "QUINCE ", "DIECISEIS", "DIECISIETE", "DIECIOCHO", "DIECINUEVE",
        "VEINTE ", "TREINTA ", "CUARENTA ", "CINCUENTA ", "SESENTA ", "SETENTA ", "OCHENTA ", "NOVENTA "
    };
    
    private static final String[] CENTENAS = {
        "CIENTO ", "DOSCIENTOS ", "TRESCIENTOS ", "CUATROCIENTOS ", "QUINIENTOS ",
        "SEISCIENTOS ", "SETECIENTOS ", "OCHOCIENTOS ", "NOVECIENTOS "
    };
    
    /**
     * Convierte un número a su representación en letras.
     * 
     * @param numero Número a convertir (puede tener decimales)
     * @return Representación en letras del número
     */
    public static String convertir(double numero) {
        long parteEntera = (long) Math.floor(numero);
        int centavos = (int) Math.round((numero - parteEntera) * 100);
        
        String resultado = convertirNumero(parteEntera).trim();
        
        // Manejar el plural
        if (parteEntera == 1) {
            resultado += " COLÓN CON ";
        } else {
            resultado += " COLONES CON ";
        }
        
        // Agregar céntimos
        if (centavos > 0) {
            resultado += String.format("%02d/100", centavos);
        } else {
            resultado += "00/100";
        }
        
        return resultado;
    }
    
    private static String convertirNumero(long numero) {
        if (numero == 0) {
            return "CERO ";
        }
        
        String resultado = "";
        
        // Miles de millón
        if ((numero / 1000000000) > 0) {
            resultado += convertirNumero(numero / 1000000000) + "MIL MILLONES ";
            numero = numero % 1000000000;
        }
        
        // Millones
        if ((numero / 1000000) > 0) {
            resultado += convertirNumeroMenorAMil((int) (numero / 1000000)) + "MILLONES ";
            numero = numero % 1000000;
        }
        
        // Miles
        if ((numero / 1000) > 0) {
            if (numero / 1000 == 1) {
                resultado += "MIL ";
            } else {
                resultado += convertirNumeroMenorAMil((int) (numero / 1000)) + "MIL ";
            }
            numero = numero % 1000;
        }
        
        // Centenas, decenas y unidades
        if (numero > 0) {
            resultado += convertirNumeroMenorAMil((int) numero);
        }
        
        return resultado;
    }
    
    private static String convertirNumeroMenorAMil(int numero) {
        String resultado = "";
        
        // Centenas
        if ((numero / 100) > 0) {
            if (numero == 100) {
                return "CIEN ";
            }
            resultado += CENTENAS[(numero / 100) - 1];
            numero = numero % 100;
        }
        
        // Decenas
        if (numero >= 10 && numero < 20) {
            resultado += DECENAS[numero - 10];
        } else if (numero >= 20) {
            resultado += DECENAS[8 + (numero / 10)];
            if (numero % 10 > 0) {
                resultado = resultado.substring(0, resultado.length() - 1) + "I" + UNIDADES[numero % 10];
            }
        } else if (numero > 0) {
            // Unidades
            resultado += UNIDADES[numero];
        }
        
        return resultado;
    }
}
