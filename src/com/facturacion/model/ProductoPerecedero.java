package com.facturacion.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Clase que representa un producto perecedero en el sistema.
 * Extiende de la clase Producto y añade funcionalidad para manejar fechas de vencimiento.
 */
public class ProductoPerecedero extends Producto {
    private LocalDate fechaVencimiento;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Constructor para crear un producto perecedero.
     * 
     * @param nombre Nombre del producto
     * @param descripcion Descripción detallada
     * @param precio Precio de venta
     * @param impuesto Tipo de impuesto
     * @param cantidadProducto Cantidad en inventario
     * @param numeroCodigo Número de código del producto
     * @param fechaVencimiento Fecha de vencimiento en formato "dd/MM/yyyy"
     */
    public ProductoPerecedero(String nombre, String descripcion, double precio, 
                             Impuesto impuesto, int cantidadProducto, String numeroCodigo, 
                             String fechaVencimiento) {
        super(nombre, descripcion, precio, impuesto, cantidadProducto, numeroCodigo);
        setFechaVencimiento(fechaVencimiento);
    }

    /**
     * Obtiene la fecha de vencimiento del producto.
     * @return Fecha de vencimiento formateada como String
     */
    public String getFechaVencimiento() {
        return fechaVencimiento.format(FORMATO_FECHA);
    }

    /**
     * Establece la fecha de vencimiento del producto.
     * @param fechaVencimiento Fecha en formato "dd/MM/yyyy"
     * @throws IllegalArgumentException Si la fecha es inválida o ya pasó
     */
    public void setFechaVencimiento(String fechaVencimiento) {
        try {
            LocalDate fecha = LocalDate.parse(fechaVencimiento, FORMATO_FECHA);
            if (fecha.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la fecha actual");
            }
            this.fechaVencimiento = fecha;
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de fecha inválido. Use dd/MM/yyyy", e);
        }
    }
    
    /**
     * Verifica si el producto está vencido.
     * @return true si el producto está vencido, false en caso contrario
     */
    public boolean estaVencido() {
        return LocalDate.now().isAfter(fechaVencimiento);
    }
    
    @Override
    public String getTipoProducto() {
        return "Perecedero";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProductoPerecedero that = (ProductoPerecedero) o;
        return Objects.equals(fechaVencimiento, that.fechaVencimiento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fechaVencimiento);
    }

    @Override
    public String toString() {
        return String.format("%s [Vence: %s] - %s", 
            super.toString(), 
            getFechaVencimiento(),
            estaVencido() ? "¡VENCIDO!" : "");
    }
}
