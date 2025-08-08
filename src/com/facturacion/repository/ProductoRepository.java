package com.facturacion.repository;

import com.facturacion.model.Producto;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz para el repositorio de productos.
 * Define las operaciones CRUD básicas para la entidad Producto.
 */
public interface ProductoRepository {
    
    /**
     * Guarda un producto en el repositorio.
     * 
     * @param producto Producto a guardar
     * @return El producto guardado, o null si no se pudo guardar
     */
    Producto guardar(Producto producto);
    
    /**
     * Busca un producto por su código.
     * 
     * @param codigo Código del producto a buscar
     * @return Un Optional con el producto si se encuentra, o vacío si no
     */
    Optional<Producto> buscarPorCodigo(int codigo);
    
    /**
     * Busca todos los productos en el repositorio.
     * 
     * @return Lista de todos los productos
     */
    List<Producto> buscarTodos();
    
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
     * @return Un Optional con el producto si se encuentra, o vacío si no
     */
    Optional<Producto> buscarPorId(int id);
    
    /**
     * Actualiza un producto existente.
     * 
     * @param producto Producto con los datos actualizados
     * @return true si se actualizó correctamente, false en caso contrario
     */
    boolean actualizar(Producto producto);
    
    /**
     * Elimina un producto por su código.
     * 
     * @param codigo Código del producto a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    boolean eliminar(int codigo);
    
    /**
     * Verifica si existe un producto con el código especificado.
     * 
     * @param codigo Código a verificar
     * @return true si existe un producto con ese código, false en caso contrario
     */
    boolean existePorCodigo(int codigo);
    
    /**
     * Elimina todos los productos del repositorio.
     * ¡Cuidado! Esta operación no se puede deshacer.
     */
    void eliminarTodos();
}
