package com.facturacion.model;

import java.util.Objects;

/**
 * Clase abstracta que representa un producto en el sistema.
 * Define la estructura común para todos los tipos de productos.
 */
public abstract class Producto {
    private static int contadorCodigo = 1;
    
    private final int codigo;
    private String nombre;
    private String descripcion;
    private double precio;
    private Impuesto impuesto;
    private int cantidadProducto;
    private String numeroCodigo;

    /**
     * Constructor para la clase Producto.
     * 
     * @param nombre Nombre del producto
     * @param descripcion Descripción detallada del producto
     * @param precioVenta Precio de venta del producto
     * @param impuesto Tipo de impuesto que aplica al producto
     * @param cantidadDisponible Cantidad disponible en inventario
     */
    public Producto(String nombre, String descripcion, double precio, 
                   Impuesto impuesto, int cantidadProducto, String numeroCodigo) {
        this.codigo = contadorCodigo++;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.impuesto = impuesto;
        this.cantidadProducto = cantidadProducto;
        this.numeroCodigo = numeroCodigo;
    }

    // Getters y Setters
    public int getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        if (precio >= 0) {
            this.precio = precio;
        }
    }

    public Impuesto getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(Impuesto impuesto) {
        this.impuesto = impuesto;
    }

    public int getCantidadProducto() {
        return cantidadProducto;
    }
    
    public String getNumeroCodigo() {
        return numeroCodigo;
    }
    
    public void setNumeroCodigo(String numeroCodigo) {
        this.numeroCodigo = numeroCodigo;
    }

    /**
     * Ajusta la cantidad disponible del producto.
     * @param cantidad La cantidad a agregar (positiva) o quitar (negativa)
     * @return true si la operación fue exitosa, false si no hay suficiente inventario
     */
    public boolean ajustarCantidad(int cantidad) {
        if (this.cantidadProducto + cantidad < 0) {
            return false; // No hay suficiente inventario
        }
        this.cantidadProducto += cantidad;
        return true;
    }
    
    /**
     * Calcula el precio total incluyendo impuestos para una cantidad dada.
     * @param cantidad Cantidad de productos
     * @return Precio total incluyendo impuestos
     */
    public double calcularPrecioTotal(int cantidad) {
        if (cantidad <= 0 || cantidad > cantidadProducto) {
            throw new IllegalArgumentException("Cantidad inválida o insuficiente en inventario");
        }
        double subtotal = precio * cantidad;
        double montoImpuesto = impuesto.calcularImpuesto(subtotal);
        return subtotal + montoImpuesto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Producto producto = (Producto) o;
        return codigo == producto.codigo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return String.format("%d - %s (Disponibles: %d, Precio: %.2f, %s, Código: %s)", 
            codigo, nombre, cantidadProducto, precio, impuesto.getDescripcion(), numeroCodigo);
    }
    
    /**
     * Método abstracto para obtener el tipo de producto.
     * @return String que identifica el tipo de producto
     */
    public abstract String getTipoProducto();
}
