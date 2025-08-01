package com.facturacion.service;

import com.facturacion.model.Cliente;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz para el servicio de gestión de clientes.
 * Define las operaciones de negocio relacionadas con los clientes.
 */
public interface ClienteService {
    
    /**
     * Registra un nuevo cliente en el sistema.
     * 
     * @param cliente Cliente a registrar
     * @return El cliente registrado con su ID asignado
     * @throws IllegalArgumentException Si el cliente es nulo o ya existe
     */
    Cliente registrarCliente(Cliente cliente);
    
    /**
     * Busca un cliente por su cédula.
     * 
     * @param cedula Cédula del cliente a buscar
     * @return Un Optional con el cliente si se encuentra, o vacío si no
     */
    Optional<Cliente> buscarPorCedula(long cedula);
    
    /**
     * Busca todos los clientes registrados.
     * 
     * @return Lista de todos los clientes
     */
    List<Cliente> listarTodos();
    
    /**
     * Busca clientes por nombre (búsqueda parcial).
     * 
     * @param nombre Nombre o parte del nombre a buscar
     * @return Lista de clientes que coinciden con la búsqueda
     */
    List<Cliente> buscarPorNombre(String nombre);
    
    /**
     * Actualiza los datos de un cliente existente.
     * 
     * @param cliente Cliente con los datos actualizados
     * @return El cliente actualizado
     * @throws IllegalArgumentException Si el cliente no existe
     */
    Cliente actualizarCliente(Cliente cliente);
    
    /**
     * Elimina un cliente por su cédula.
     * 
     * @param cedula Cédula del cliente a eliminar
     * @return true si se eliminó correctamente, false si no existía
     */
    boolean eliminarCliente(long cedula);
    
    /**
     * Verifica si un cliente es Ciudadano de Oro.
     * 
     * @param cedula Cédula del cliente a verificar
     * @return true si el cliente es Ciudadano de Oro, false en caso contrario
     */
    boolean esCiudadanoOro(long cedula);
    
    /**
     * Actualiza los puntos de un cliente.
     * 
     * @param cedula Cédula del cliente
     * @param puntos Puntos a sumar (pueden ser negativos para restar)
     * @return true si se actualizaron los puntos correctamente, false si el cliente no existe
     */
    boolean actualizarPuntos(long cedula, int puntos);
    
    /**
     * Obtiene la cantidad de puntos de un cliente.
     * 
     * @param cedula Cédula del cliente
     * @return Cantidad de puntos del cliente, o -1 si no existe
     */
    int obtenerPuntos(long cedula);
}
