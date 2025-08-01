package com.facturacion.service.impl;

import com.facturacion.model.Cliente;
import com.facturacion.model.TipoCliente;
import com.facturacion.repository.ClienteRepository;
import com.facturacion.service.ClienteService;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de gestión de clientes.
 */
public class ClienteServiceImpl implements ClienteService {
    
    private static final int PUNTOS_POR_COMPRA = 10; // Puntos por cada compra
    private static final int EDAD_MINIMA_CIUDADANO_ORO = 65; // Edad mínima para ser Ciudadano de Oro
    
    private final ClienteRepository clienteRepository;
    
    /**
     * Constructor que recibe el repositorio de clientes.
     * 
     * @param clienteRepository Repositorio de clientes
     * @throws IllegalArgumentException Si el repositorio es nulo
     */
    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        if (clienteRepository == null) {
            throw new IllegalArgumentException("El repositorio de clientes no puede ser nulo");
        }
        this.clienteRepository = clienteRepository;
    }
    
    @Override
    public Cliente registrarCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }
        
        // Validar que no exista un cliente con la misma cédula
        if (clienteRepository.existePorCedula(cliente.getCedula())) {
            throw new IllegalArgumentException("Ya existe un cliente con la cédula: " + cliente.getCedula());
        }
        
        // Si es un cliente ocasional, verificar si califica como Ciudadano de Oro
        if (cliente.getTipo() == TipoCliente.OCASIONALES) {
            boolean esCiudadanoOro = validarCiudadanoOro(cliente);
            cliente.setCiudadanoOro(esCiudadanoOro);
        }
        
        // Validar que el cliente tenga los datos requeridos
        validarCliente(cliente);
        
        // Guardar el cliente
        Cliente clienteGuardado = clienteRepository.guardar(cliente);
        if (clienteGuardado == null) {
            throw new RuntimeException("Error al guardar el cliente con cédula: " + cliente.getCedula());
        }
        
        return cliente;
    }
    
    /**
     * Valida si un cliente ocasional califica como Ciudadano de Oro.
     * 
     * @param cliente Cliente a validar
     * @return true si el cliente es Ciudadano de Oro, false en caso contrario
     */
    private boolean validarCiudadanoOro(Cliente cliente) {
        // Aquí se implementaría la lógica para determinar si un cliente es Ciudadano de Oro
        // Por ejemplo, podría basarse en la edad, historial de compras, etc.
        // Por ahora, lo dejamos como falso por defecto
        return false;
    }
    
    @Override
    public Optional<Cliente> buscarPorCedula(long cedula) {
        if (cedula <= 0) {
            throw new IllegalArgumentException("La cédula debe ser un número positivo");
        }
        return clienteRepository.buscarPorCedula(cedula);
    }
    
    @Override
    public List<Cliente> listarTodos() {
        return clienteRepository.buscarTodos();
    }
    
    @Override
    public List<Cliente> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        return clienteRepository.buscarPorNombre(nombre.trim());
    }
    
    @Override
    public Cliente actualizarCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }
        
        // Validar que el cliente exista
        if (!clienteRepository.existePorCedula(cliente.getCedula())) {
            throw new IllegalArgumentException("No existe un cliente con la cédula: " + cliente.getCedula());
        }
        
        // Validar los datos del cliente
        validarCliente(cliente);
        
        // Actualizar el cliente
        if (!clienteRepository.actualizar(cliente)) {
            throw new RuntimeException("Error al actualizar el cliente con cédula: " + cliente.getCedula());
        }
        
        return cliente;
    }
    
    @Override
    public boolean eliminarCliente(long cedula) {
        if (cedula <= 0) {
            throw new IllegalArgumentException("La cédula debe ser un número positivo");
        }
        return clienteRepository.eliminar(cedula);
    }
    
    @Override
    public boolean esCiudadanoOro(long cedula) {
        if (cedula <= 0) {
            throw new IllegalArgumentException("La cédula debe ser un número positivo");
        }
        return buscarPorCedula(cedula)
            .map(Cliente::isCiudadanoOro)
            .orElse(false);
    }
    
    @Override
    public boolean actualizarPuntos(long cedula, int puntos) {
        if (cedula <= 0) {
            throw new IllegalArgumentException("La cédula debe ser un número positivo");
        }
        if (puntos == 0) {
            return true; // No hay nada que actualizar
        }
        
        return buscarPorCedula(cedula)
            .map(cliente -> {
                try {
                    if (puntos > 0) {
                        cliente.agregarPuntos(puntos);
                    } else {
                        // Intentar canjear puntos si son negativos
                        int puntosACanjear = -puntos;
                        if (!cliente.canjearPuntos(puntosACanjear)) {
                            return false; // No hay suficientes puntos para canjear
                        }
                    }
                    return clienteRepository.actualizar(cliente);
                } catch (IllegalArgumentException e) {
                    return false; // Error al actualizar los puntos
                }
            })
            .orElse(false);
    }
    
    @Override
    public int obtenerPuntos(long cedula) {
        if (cedula <= 0) {
            throw new IllegalArgumentException("La cédula debe ser un número positivo");
        }
        return buscarPorCedula(cedula)
            .map(Cliente::getPuntos)
            .orElse(-1);
    }
    
    /**
     * Valida que los datos obligatorios del cliente sean correctos.
     * 
     * @param cliente Cliente a validar
     * @throws IllegalArgumentException Si algún dato es inválido
     */
    private void validarCliente(Cliente cliente) {
        if (cliente.getCedula() <= 0) {
            throw new IllegalArgumentException("La cédula debe ser un número positivo");
        }
        
        if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        
        if (cliente.getTipo() == null) {
            throw new IllegalArgumentException("El tipo de cliente es obligatorio");
        }
        
        // Validar que los clientes corporativos tengan un contacto
        if (cliente.getTipo() == TipoCliente.CORPORATIVOS && 
            (cliente.getContacto() == null || cliente.getContacto().trim().isEmpty())) {
            throw new IllegalArgumentException("El contacto es obligatorio para clientes corporativos");
        }
        
        if (cliente.getPuntos() < 0) {
            throw new IllegalArgumentException("Los puntos no pueden ser negativos");
        }
    }
}
