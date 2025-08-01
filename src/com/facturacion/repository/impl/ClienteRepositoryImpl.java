package com.facturacion.repository.impl;

import com.facturacion.model.Cliente;
import com.facturacion.model.TipoCliente;
import com.facturacion.repository.ClienteRepository;
import com.facturacion.util.JsonUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación de ClienteRepository que almacena los datos en archivos JSON.
 */
public class ClienteRepositoryImpl implements ClienteRepository {
    
    private static final String DATA_DIR = "data";
    private static final String CLIENTES_DIR = DATA_DIR + File.separator + "clientes";
    
    public ClienteRepositoryImpl() {
        // Crear directorios si no existen
        try {
            Files.createDirectories(Paths.get(CLIENTES_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Error al inicializar el repositorio de clientes", e);
        }
    }
    
    private Path getClientePath(long cedula) {
        return Paths.get(CLIENTES_DIR, cedula + ".json");
    }
    
    @Override
    public Cliente guardar(Cliente cliente) {
        if (cliente == null) {
            return null;
        }
        
        try {
            Path filePath = getClientePath(cliente.getCedula());
            String json = JsonUtil.toJson(cliente);
            Files.writeString(filePath, json);
            return cliente;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el cliente", e);
        }
    }
    
    @Override
    public Optional<Cliente> buscarPorCedula(long cedula) {
        Path filePath = getClientePath(cedula);
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        
        try {
            String json = Files.readString(filePath);
            return Optional.of(JsonUtil.fromJson(json, Cliente.class));
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el cliente con cédula: " + cedula, e);
        }
    }
    
    @Override
    public List<Cliente> buscarTodos() {
        try {
            return Files.list(Paths.get(CLIENTES_DIR))
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json"))
                .map(path -> {
                    try {
                        String json = Files.readString(path);
                        return JsonUtil.fromJson(json, Cliente.class);
                    } catch (IOException e) {
                        throw new RuntimeException("Error al leer el archivo: " + path, e);
                    }
                })
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error al listar los clientes", e);
        }
    }
    
    @Override
    public List<Cliente> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String busqueda = nombre.toLowerCase();
        return buscarTodos().stream()
            .filter(cliente -> cliente.getNombre().toLowerCase().contains(busqueda))
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean actualizar(Cliente cliente) {
        if (cliente == null || !existePorCedula(cliente.getCedula())) {
            return false;
        }
        
        try {
            // Guardar el cliente actualizado (sobreescribe el archivo existente)
            guardar(cliente);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el cliente con cédula: " + cliente.getCedula(), e);
        }
    }
    
    @Override
    public boolean eliminar(long cedula) {
        try {
            Path filePath = getClientePath(cedula);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar el cliente con cédula: " + cedula, e);
        }
    }
    
    @Override
    public boolean existePorCedula(long cedula) {
        return Files.exists(getClientePath(cedula));
    }
}
