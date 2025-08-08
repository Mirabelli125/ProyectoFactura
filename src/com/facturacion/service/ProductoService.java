package com.facturacion.service;

import com.facturacion.model.Producto;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz para el servicio de gestión de productos.
 * Define las operaciones de negocio relacionadas con los productos.
 */
public interface ProductoService {
    
    /**
     * Registra un nuevo producto en el sistema.
     * 
     * @param producto Producto a registrar
     * @return El producto registrado con su código asignado
     * @throws IllegalArgumentException Si el producto es nulo o ya existe
     */
    Producto registrarProducto(Producto producto);
    
    /**
     * Busca un producto por su código.
     * 
     * @param codigo Código del producto a buscar
     * @return Un Optional con el producto si se encuentra, o vacío si no
     */
    Optional<Producto> buscarPorCodigo(int codigo);
    
    /**
     * Busca todos los productos registrados.
     * 
     * @return Lista de todos los productos
     */
    List<Producto> listarTodos();
    
    /**
     * Busca productos por nombre (búsqueda parcial).
     * 
     * @param nombre Nombre o parte del nombre a buscar
     * @return Lista de productos que coinciden con la búsqueda
     */
    List<Producto> buscarPorNombre(String nombre);
    
    /**
     * Busca productos por tipo (perecedero o no perecedero).
     * 
     * @param esPerecedero true para buscar productos perecederos, false para no perecederos
     * @return Lista de productos que coinciden con el tipo
     */
    List<Producto> buscarPorTipo(boolean esPerecedero);
    
    /**
     * Busca un producto por su ID.
     * 
     * @param id ID del producto a buscar
     * @return El producto encontrado, o null si no se encuentra
     */
    Producto buscarProductoPorId(int id);
    
    /**
     * Actualiza los datos de un producto existente.
     * 
     * @param producto Producto con los datos actualizados
     * @return El producto actualizado
     * @throws IllegalArgumentException Si el producto no existe
     */
    Producto actualizarProducto(Producto producto);
    
    /**
     * Elimina un producto por su código.
     * 
     * @param codigo Código del producto a eliminar
     * @return true si se eliminó correctamente, false si no existía
     */
    boolean eliminarProducto(int codigo);
    
    /**
     * Verifica si un producto tiene suficiente inventario disponible.
     * 
     * @param codigo Código del producto
     * @param cantidad Cantidad requerida
     * @return true si hay suficiente inventario, false en caso contrario
     */
    boolean tieneInventarioSuficiente(int codigo, int cantidad);
    
    /**
     * Actualiza el inventario de un producto.
     * 
     * @param codigo Código del producto
     * @param cantidad Cantidad a sumar (puede ser negativa para restar)
     * @return true si se actualizó correctamente, false si no existe el producto o no hay suficiente inventario
     */
    boolean actualizarInventario(int codigo, int cantidad);
    
    /**
     * Obtiene el precio de un producto, aplicando impuestos si es necesario.
     * 
     * @param codigo Código del producto
     * @param incluirImpuestos true para incluir impuestos en el precio
     * @return El precio del producto, o -1 si no existe
     */
    double obtenerPrecio(int codigo, boolean incluirImpuestos);
}
