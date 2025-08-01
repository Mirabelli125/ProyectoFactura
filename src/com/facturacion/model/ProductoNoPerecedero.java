package com.facturacion.model;

/**
 * Clase que representa un producto no perecedero en el sistema.
 * Extiende de la clase Producto.
 */
public class ProductoNoPerecedero extends Producto {
    
    /**
     * Constructor para crear un producto no perecedero.
     * 
     * @param nombre Nombre del producto
     * @param descripcion Descripción detallada
     * @param precio Precio de venta
     * @param impuesto Tipo de impuesto
     * @param cantidadProducto Cantidad en inventario
     * @param numeroCodigo Número de código del producto
     */
    public ProductoNoPerecedero(String nombre, String descripcion, double precio, 
                              Impuesto impuesto, int cantidadProducto, String numeroCodigo) {
        super(nombre, descripcion, precio, impuesto, cantidadProducto, numeroCodigo);
    }
    
    @Override
    public String getTipoProducto() {
        return "No Perecedero";
    }
    
    @Override
    public String toString() {
        return super.toString();
    }
}
