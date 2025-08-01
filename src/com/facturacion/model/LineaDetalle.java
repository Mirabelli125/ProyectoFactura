package com.facturacion.model;

import java.util.Objects;

/**
 * Clase que representa una línea de detalle en una factura.
 * Contiene información sobre un producto específico y la cantidad comprada.
 */
public class LineaDetalle {
    private final int numeroLinea;
    private int cantidadProducto;
    private final Producto producto;
    private double subtotal;
    private double impuesto;

    /**
     * Constructor para crear una línea de detalle.
     * 
     * @param numeroLinea Número secuencial de la línea en la factura
     * @param producto Producto que se está facturando
     * @param cantidad Cantidad del producto
     */
    public LineaDetalle(int numeroLinea, Producto producto, int cantidadProducto) {
        if (cantidadProducto <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }
        if (cantidadProducto > producto.getCantidadProducto()) {
            throw new IllegalArgumentException("No hay suficiente inventario para este producto");
        }
        
        this.numeroLinea = numeroLinea;
        this.producto = producto;
        this.cantidadProducto = cantidadProducto;
        
        // Calcular valores iniciales
        calcularValores();
    }

    /**
     * Calcula los valores de subtotal, impuesto y total de la línea.
     */
    private void calcularValores() {
        this.subtotal = producto.getPrecio() * cantidadProducto;
        this.impuesto = producto.getImpuesto().calcularImpuesto(subtotal);
    }

    // Getters
    public int getNumeroLinea() {
        return numeroLinea;
    }

    public int getCantidadProducto() {
        return cantidadProducto;
    }

    /**
     * Actualiza la cantidad del producto en la línea.
     * @param nuevaCantidad Nueva cantidad
     * @return true si la actualización fue exitosa, false si no hay suficiente inventario
     */
    public boolean setCantidadProducto(int nuevaCantidad) {
        if (nuevaCantidad <= 0) {
            return false;
        }
        
        // Verificar si hay suficiente inventario para el cambio
        int diferencia = nuevaCantidad - this.cantidadProducto;
        if (diferencia > 0 && (producto.getCantidadProducto() < diferencia)) {
            return false; // No hay suficiente inventario
        }
        
        this.cantidadProducto = nuevaCantidad;
        calcularValores();
        return true;
    }

    public Producto getProducto() {
        return producto;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getImpuesto() {
        return impuesto;
    }

    public double getTotalLinea() {
        return subtotal + impuesto;
    }
    
    /**
     * Incrementa la cantidad en 1 si hay inventario disponible.
     * @return true si se pudo incrementar, false si no hay suficiente inventario
     */
    public boolean incrementarCantidad() {
        return setCantidadProducto(cantidadProducto + 1);
    }
    
    /**
     * Decrementa la cantidad en 1 si la cantidad resultante es mayor a 0.
     * @return true si se pudo decrementar, false si la cantidad llegaría a 0
     */
    public boolean decrementarCantidad() {
        if (cantidadProducto <= 1) {
            return false;
        }
        return setCantidadProducto(cantidadProducto - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineaDetalle that = (LineaDetalle) o;
        return numeroLinea == that.numeroLinea && 
               producto.equals(that.producto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroLinea, producto);
    }

    @Override
    public String toString() {
        return String.format("%d. %-40s %4d x %8.2f %8.2f %8.2f %8.2f",
            numeroLinea,
            producto.getNombre().substring(0, Math.min(40, producto.getNombre().length())),
            cantidadProducto,
            producto.getPrecio(),
            subtotal,
            impuesto,
            getTotalLinea());
    }
}
