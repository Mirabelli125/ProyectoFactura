package com.facturacion.repository.impl;

import com.facturacion.model.Producto;
import com.facturacion.model.ProductoPerecedero;
import com.facturacion.repository.ProductoRepository;
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
 * Implementación de ProductoRepository que almacena los datos en archivos JSON.
 */
public class ProductoRepositoryImpl implements ProductoRepository {
    
    private static final String DATA_DIR = "data";
    private static final String PRODUCTOS_DIR = DATA_DIR + File.separator + "productos";
    
    public ProductoRepositoryImpl() {
        // Crear directorios si no existen
        try {
            Files.createDirectories(Paths.get(PRODUCTOS_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Error al inicializar el repositorio de productos", e);
        }
    }
    
    private Path getProductoPath(int codigo) {
        return Paths.get(PRODUCTOS_DIR, codigo + ".json");
    }
    
    @Override
    public Producto guardar(Producto producto) {
        if (producto == null) {
            return null;
        }
        
        try {
            Path filePath = getProductoPath(producto.getCodigo());
            String json = JsonUtil.toJson(producto);
            Files.writeString(filePath, json);
            return producto;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el producto", e);
        }
    }
    
    @Override
    public Optional<Producto> buscarPorCodigo(int codigo) {
        Path filePath = getProductoPath(codigo);
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        
        try {
            String json = Files.readString(filePath);
            // Determinar el tipo de producto para la deserialización correcta
            if (json.contains("fechaVencimiento")) {
                return Optional.of(JsonUtil.fromJson(json, ProductoPerecedero.class));
            } else {
                return Optional.of(JsonUtil.fromJson(json, Producto.class));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el producto con código: " + codigo, e);
        }
    }
    
    @Override
    public List<Producto> buscarTodos() {
        try {
            return Files.list(Paths.get(PRODUCTOS_DIR))
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json"))
                .map(path -> {
                    try {
                        String json = Files.readString(path);
                        // Determinar el tipo de producto para la deserialización correcta
                        if (json.contains("fechaVencimiento")) {
                            return JsonUtil.fromJson(json, ProductoPerecedero.class);
                        } else {
                            return JsonUtil.fromJson(json, Producto.class);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Error al leer el archivo: " + path, e);
                    }
                })
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error al listar los productos", e);
        }
    }
    
    @Override
    public List<Producto> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String busqueda = nombre.toLowerCase();
        return buscarTodos().stream()
            .filter(producto -> producto.getNombre().toLowerCase().contains(busqueda))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Producto> buscarPorTipo(boolean esPerecedero) {
        return buscarTodos().stream()
            .filter(p -> p instanceof ProductoPerecedero == esPerecedero)
            .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Producto> buscarPorId(int id) {
        // In this implementation, we'll use the code as the ID since they're equivalent in this context
        return buscarPorCodigo(id);
    }
    
    @Override
    public boolean actualizar(Producto producto) {
        if (producto == null || !existePorCodigo(producto.getCodigo())) {
            return false;
        }
        
        try {
            // Guardar el producto actualizado (sobreescribe el archivo existente)
            guardar(producto);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el producto con código: " + producto.getCodigo(), e);
        }
    }
    
    @Override
    public boolean eliminar(int codigo) {
        try {
            Path filePath = getProductoPath(codigo);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar el producto con código: " + codigo, e);
        }
    }
    
    @Override
    public boolean existePorCodigo(int codigo) {
        return Files.exists(getProductoPath(codigo));
    }
}
