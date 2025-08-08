package com.facturacion.repository;

import com.facturacion.model.Cliente;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz para el repositorio de clientes.
 * Define las operaciones CRUD básicas para la entidad Cliente.
 */
public interface ClienteRepository {
    
    /**
     * Guarda un cliente en el repositorio.
     * 
     * @param cliente Cliente a guardar
     * @return El cliente guardado, o null si no se pudo guardar
     */
    Cliente guardar(Cliente cliente);
    
    /**
     * Busca un cliente por su cédula.
     * 
     * @param cedula Cédula del cliente a buscar
     * @return Un Optional con el cliente si se encuentra, o vacío si no
     */
    Optional<Cliente> buscarPorCedula(long cedula);
    
    /**
     * Busca todos los clientes en el repositorio.
     * 
     * @return Lista de todos los clientes
     */
    List<Cliente> buscarTodos();
    
    /**
     * Busca clientes por nombre (búsqueda parcial).
     * 
     * @param nombre Nombre o parte del nombre a buscar
     * @return Lista de clientes que coinciden con la búsqueda
     */
    List<Cliente> buscarPorNombre(String nombre);
    
    /**
     * Actualiza un cliente existente.
     * 
     * @param cliente Cliente con los datos actualizados
     * @return true si se actualizó correctamente, false en caso contrario
     */
    boolean actualizar(Cliente cliente);
    
    /**
     * Elimina un cliente por su cédula.
     * 
     * @param cedula Cédula del cliente a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    boolean eliminar(long cedula);
    
    /**
     * Verifica si existe un cliente con la cédula especificada.
     * 
     * @param cedula Cédula a verificar
     * @return true si existe un cliente con esa cédula, false en caso contrario
     */
    boolean existePorCedula(long cedula);
    
    /**
     * Elimina todos los clientes del repositorio.
     * ¡Cuidado! Esta operación no se puede deshacer.
     */
    void eliminarTodos();
}
