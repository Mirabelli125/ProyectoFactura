package com.facturacion.service.impl;

import com.facturacion.model.Impuesto;
import com.facturacion.model.Producto;
import com.facturacion.model.ProductoPerecedero;
import com.facturacion.repository.ProductoRepository;
import com.facturacion.service.ProductoService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de productos.
 */
public class ProductoServiceImpl implements ProductoService {
    
    private final ProductoRepository productoRepository;
    
    /**
     * Constructor que recibe el repositorio de productos.
     * 
     * @param productoRepository Repositorio de productos
     * @throws IllegalArgumentException Si el repositorio es nulo
     */
    public ProductoServiceImpl(ProductoRepository productoRepository) {
        if (productoRepository == null) {
            throw new IllegalArgumentException("El repositorio de productos no puede ser nulo");
        }
        this.productoRepository = productoRepository;
    }
    
    @Override
    public Producto registrarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }
        
        validarProducto(producto);
        
        // Validar que no exista un producto con el mismo código
        if (producto.getCodigo() > 0 && productoRepository.existePorCodigo(producto.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un producto con el código: " + producto.getCodigo());
        }
        
        // Validar datos obligatorios
        validarProducto(producto);
        
        // Si es un producto perecedero, validar la fecha de vencimiento
        if (producto instanceof ProductoPerecedero) {
            validarProductoPerecedero((ProductoPerecedero) producto);
        }
        
        // Guardar el producto
        return productoRepository.guardar(producto);
    }
    
    @Override
    public Optional<Producto> buscarPorCodigo(int codigo) {
        if (codigo <= 0) {
            return Optional.empty();
        }
        return productoRepository.buscarPorCodigo(codigo);
    }
    
    @Override
    public List<Producto> listarTodos() {
        return productoRepository.buscarTodos();
    }
    
    @Override
    public List<Producto> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        return productoRepository.buscarPorNombre(nombre);
    }
    
    @Override
    public List<Producto> buscarPorTipo(boolean esPerecedero) {
        return productoRepository.buscarPorTipo(esPerecedero);
    }
    
    @Override
    public Producto actualizarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }
        
        // Validar que el producto exista
        if (!productoRepository.existePorCodigo(producto.getCodigo())) {
            throw new IllegalArgumentException("No existe un producto con el código: " + producto.getCodigo());
        }
        
        // Validar datos obligatorios
        validarProducto(producto);
        
        // Si es un producto perecedero, validar la fecha de vencimiento
        if (producto instanceof ProductoPerecedero) {
            validarProductoPerecedero((ProductoPerecedero) producto);
        }
        
        // Actualizar el producto
        if (!productoRepository.actualizar(producto)) {
            throw new RuntimeException("Error al actualizar el producto con código: " + producto.getCodigo());
        }
        
        return producto;
    }
    
    @Override
    public boolean eliminarProducto(int codigo) {
        if (codigo <= 0) {
            return false;
        }
        return productoRepository.eliminar(codigo);
    }
    
    @Override
    public boolean tieneInventarioSuficiente(int codigo, int cantidad) {
        if (codigo <= 0 || cantidad <= 0) {
            return false;
        }
        
        return buscarPorCodigo(codigo)
            .map(producto -> producto.getCantidadProducto() >= cantidad)
            .orElse(false);
    }
    
    @Override
    public boolean actualizarInventario(int codigo, int cantidad) {
        if (codigo <= 0) {
            return false;
        }
        
        return buscarPorCodigo(codigo)
            .map(producto -> {
                int nuevaCantidad = producto.getCantidadProducto() + cantidad;
                if (nuevaCantidad < 0) {
                    return false; // No permitir cantidades negativas
                }
                
                try {
                    // Usar reflexión para acceder al campo cantidadProducto
                    java.lang.reflect.Field field = Producto.class.getDeclaredField("cantidadProducto");
                    field.setAccessible(true);
                    field.setInt(producto, nuevaCantidad);
                    return actualizarProducto(producto) != null;
                } catch (Exception e) {
                    throw new RuntimeException("Error al actualizar el inventario", e);
                }
            })
            .orElse(false);
    }
    
    @Override
    public double obtenerPrecio(int codigo, boolean incluirImpuestos) {
        if (codigo <= 0) {
            return -1;
        }
        
        return buscarPorCodigo(codigo)
            .map(producto -> {
                if (incluirImpuestos) {
                    double subtotal = producto.getPrecio();
                    return subtotal + (subtotal * producto.getImpuesto().getPorcentaje() / 100);
                }
                return producto.getPrecio();
            })
            .orElse(-1.0);
    }
    
    /**
     * Valida que los datos obligatorios del producto sean correctos.
     * 
     * @param producto Producto a validar
     * @throws IllegalArgumentException Si algún dato es inválido
     */
    private void validarProducto(Producto producto) {
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        
        try {
            // Usar reflexión para obtener el campo precio
            java.lang.reflect.Field precioField = Producto.class.getDeclaredField("precio");
            precioField.setAccessible(true);
            double precio = precioField.getDouble(producto);
            
            if (precio <= 0) {
                throw new IllegalArgumentException("El precio debe ser mayor que cero");
            }
            
            // Usar reflexión para obtener el campo cantidadProducto
            java.lang.reflect.Field cantidadField = Producto.class.getDeclaredField("cantidadProducto");
            cantidadField.setAccessible(true);
            int cantidad = cantidadField.getInt(producto);
            
            if (cantidad < 0) {
                throw new IllegalArgumentException("La cantidad de producto no puede ser negativa");
            }
            
            if (producto.getImpuesto() == null) {
                throw new IllegalArgumentException("El impuesto es obligatorio");
            }
            
            // Validar que el impuesto sea uno de los valores permitidos
            Impuesto impuesto = producto.getImpuesto();
            boolean esValido = false;
            for (Impuesto i : Impuesto.values()) {
                if (i == impuesto) {
                    esValido = true;
                    break;
                }
            }
            if (!esValido) {
                throw new IllegalArgumentException("Tipo de impuesto no válido");
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Error al validar el producto: " + e.getMessage(), e);
        }
    }
    
    /**
     * Valida los datos específicos de un producto perecedero.
     * 
     * @param producto Producto perecedero a validar
     * @throws IllegalArgumentException Si algún dato es inválido
     */
    private void validarProductoPerecedero(ProductoPerecedero producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }
        
        String fechaStr = producto.getFechaVencimiento();
        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            throw new IllegalArgumentException("La fecha de vencimiento es obligatoria para productos perecederos");
        }
        
        try {
            // Try parsing with ISO format (yyyy-MM-dd) first
            LocalDate fechaVencimiento;
            try {
                fechaVencimiento = LocalDate.parse(fechaStr);
            } catch (DateTimeParseException e) {
                // If ISO format fails, try with common format (dd/MM/yyyy)
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                fechaVencimiento = LocalDate.parse(fechaStr, formatter);
            }
            
            LocalDate hoy = LocalDate.now();
            if (fechaVencimiento.isBefore(hoy)) {
                throw new IllegalArgumentException("La fecha de vencimiento " + fechaStr + " ya ha pasado");
            }
            
            // Optional: Validate that the date is not too far in the future (e.g., 10 years)
            LocalDate maxFecha = hoy.plusYears(10);
            if (fechaVencimiento.isAfter(maxFecha)) {
                throw new IllegalArgumentException("La fecha de vencimiento no puede ser posterior a " + maxFecha.format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
            
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha inválido. Use yyyy-MM-dd o dd/MM/yyyy: " + fechaStr, e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al validar la fecha de vencimiento: " + e.getMessage(), e);
        }
    }
}
