package com.facturacion.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// No need to import classes from the same package

/**
 * Clase que representa la empresa que utiliza el sistema de facturación.
 * Gestiona clientes, productos y facturas.
 */
public class Empresa {
    private final long cedulaJuridica;
    private final String nombreEmpresa;
    private final List<Cliente> clientes;
    private final List<Producto> productos;
    private final List<Factura> facturas;
    private Cliente clienteContado; // Cliente por defecto para ventas sin registro

    /**
     * Constructor para crear una instancia de Empresa.
     * 
     * @param cedulaJuridica Cédula jurídica de la empresa
     * @param nombreEmpresa Nombre de la empresa
     */
    public Empresa(long cedulaJuridica, String nombreEmpresa) {
        this.cedulaJuridica = cedulaJuridica;
        this.nombreEmpresa = nombreEmpresa;
        this.clientes = new ArrayList<>();
        this.productos = new ArrayList<>();
        this.facturas = new ArrayList<>();
        
        // Crear cliente contado por defecto (contacto puede ser nulo para clientes ocasionales)
        this.clienteContado = new Cliente(0, "CLIENTE CONTADO", TipoCliente.OCASIONALES, null);
    }

    // Getters
    public long getCedulaJuridica() {
        return cedulaJuridica;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public List<Cliente> getClientes() {
        return new ArrayList<>(clientes);
    }

    public List<Producto> getProductos() {
        return new ArrayList<>(productos);
    }

    public List<Factura> getFacturas() {
        return new ArrayList<>(facturas);
    }
    
    public Cliente getClienteContado() {
        return clienteContado;
    }
    
    // Métodos de gestión de clientes
    
    /**
     * Agrega un nuevo cliente a la empresa.
     * 
     * @param cliente Cliente a agregar
     * @return true si se agregó correctamente, false si ya existe un cliente con esa cédula
     */
    public boolean agregarCliente(Cliente cliente) {
        if (cliente == null || buscarClientePorCedula(cliente.getCedula()).isPresent()) {
            return false;
        }
        return clientes.add(cliente);
    }
    
    /**
     * Busca un cliente por su número de cédula.
     * 
     * @param cedula Número de cédula a buscar
     * @return Optional con el cliente si se encuentra, vacío en caso contrario
     */
    public Optional<Cliente> buscarClientePorCedula(long cedula) {
        return clientes.stream()
            .filter(c -> c.getCedula() == cedula)
            .findFirst();
    }
    
    /**
     * Actualiza la información de un cliente existente.
     * 
     * @param cedula Cédula del cliente a actualizar
     * @param nuevoCliente Nueva información del cliente
     * @return true si se actualizó correctamente, false si no existe el cliente
     */
    public boolean actualizarCliente(long cedula, Cliente nuevoCliente) {
        if (nuevoCliente == null || cedula != nuevoCliente.getCedula()) {
            return false;
        }
        
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getCedula() == cedula) {
                clientes.set(i, nuevoCliente);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Elimina un cliente de la empresa.
     * 
     * @param cedula Cédula del cliente a eliminar
     * @return true si se eliminó correctamente, false si no existe el cliente
     */
    public boolean eliminarCliente(long cedula) {
        return clientes.removeIf(c -> c.getCedula() == cedula);
    }
    
    // Métodos de gestión de productos
    
    /**
     * Agrega un nuevo producto al inventario.
     * 
     * @param producto Producto a agregar
     * @return true si se agregó correctamente, false si ya existe un producto con el mismo código
     */
    public boolean agregarProducto(Producto producto) {
        if (producto == null || buscarProductoPorCodigo(producto.getCodigo()).isPresent()) {
            return false;
        }
        return productos.add(producto);
    }
    
    /**
     * Busca un producto por su código.
     * 
     * @param codigo Código del producto a buscar
     * @return Optional con el producto si se encuentra, vacío en caso contrario
     */
    public Optional<Producto> buscarProductoPorCodigo(int codigo) {
        return productos.stream()
            .filter(p -> p.getCodigo() == codigo)
            .findFirst();
    }
    
    /**
     * Busca productos por nombre (búsqueda parcial sin distinción de mayúsculas/minúsculas).
     * 
     * @param nombre Nombre o parte del nombre a buscar
     * @return Lista de productos que coinciden con la búsqueda
     */
    public List<Producto> buscarProductosPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String busqueda = nombre.toLowerCase();
        return productos.stream()
            .filter(p -> p.getNombre().toLowerCase().contains(busqueda))
            .collect(Collectors.toList());
    }
    
    /**
     * Actualiza la información de un producto existente.
     * 
     * @param codigo Código del producto a actualizar
     * @param productoActualizado Nueva información del producto
     * @return true si se actualizó correctamente, false si no existe el producto
     */
    public boolean actualizarProducto(int codigo, Producto productoActualizado) {
        if (productoActualizado == null || codigo != productoActualizado.getCodigo()) {
            return false;
        }
        
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getCodigo() == codigo) {
                productos.set(i, productoActualizado);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Elimina un producto del inventario.
     * 
     * @param codigo Código del producto a eliminar
     * @return true si se eliminó correctamente, false si no existe el producto
     */
    public boolean eliminarProducto(int codigo) {
        return productos.removeIf(p -> p.getCodigo() == codigo);
    }
    
    // Métodos de gestión de facturas
    
    /**
     * Crea una nueva factura para un cliente.
     * 
     * @param cedulaCliente Cédula del cliente (0 para cliente contado)
     * @return Nueva factura creada
     */
    public Factura crearFactura(long cedulaCliente) {
        Cliente cliente;
        
        if (cedulaCliente == 0) {
            cliente = clienteContado;
        } else {
            cliente = buscarClientePorCedula(cedulaCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        }
        
        Factura factura = new Factura(cliente);
        facturas.add(factura);
        return factura;
    }
    
    /**
     * Busca una factura por su número.
     * 
     * @param numeroFactura Número de factura a buscar
     * @return Optional con la factura si se encuentra, vacío en caso contrario
     */
    public Optional<Factura> buscarFactura(int numeroFactura) {
        return facturas.stream()
            .filter(f -> f.getNumeroFactura() == numeroFactura)
            .findFirst();
    }
    
    /**
     * Obtiene las facturas de un cliente específico.
     * 
     * @param cedulaCliente Cédula del cliente
     * @return Lista de facturas del cliente
     */
    public List<Factura> obtenerFacturasPorCliente(long cedulaCliente) {
        return facturas.stream()
            .filter(f -> f.getCliente().getCedula() == cedulaCliente)
            .collect(Collectors.toList());
    }
    
    /**
     * Obtiene las facturas emitidas en una fecha específica.
     * 
     * @param fecha Fecha a consultar
     * @return Lista de facturas de la fecha especificada
     */
    public List<Factura> obtenerFacturasDelDia() {
        return facturas.stream()
            .filter(f -> f.getFechaFactura().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                .isEqual(java.time.LocalDate.now()))
            .collect(Collectors.toList());
    }
    
    /**
     * Genera un informe de ventas del día.
     * 
     * @return String con el informe de ventas
     */
    public String generarInformeVentasDelDia() {
        List<Factura> facturasHoy = obtenerFacturasDelDia();
        
        if (facturasHoy.isEmpty()) {
            return "No hay ventas registradas para el día de hoy.";
        }
        
        double totalVentasColones = 0;
        double totalVentasDolares = 0;
        double totalTarjetas = 0;
        
        for (Factura factura : facturasHoy) {
            if (factura.getPago() != null) {
                if (factura.getPago().esEfectivo()) {
                    if (factura.getPago().esEnDolares()) {
                        totalVentasDolares += factura.getTotal();
                    } else {
                        totalVentasColones += factura.getTotal();
                    }
                } else {
                    totalTarjetas += factura.getTotal();
                }
            }
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("INFORME DE VENTAS - ").append(java.time.LocalDate.now()).append("\n");
        sb.append("========================================\n");
        sb.append(String.format("Total de facturas: %d%n", facturasHoy.size()));
        sb.append(String.format("Ventas en efectivo (colones): ₡%,.2f%n", totalVentasColones));
        sb.append(String.format("Ventas en efectivo (dólares): $%,.2f%n", totalVentasDolares));
        sb.append(String.format("Ventas con tarjeta: ₡%,.2f%n", totalTarjetas));
        sb.append("----------------------------------------\n");
        sb.append(String.format("TOTAL GENERAL: ₡%,.2f%n", 
            totalVentasColones + totalTarjetas + 
            (totalVentasDolares * Moneda.DOLARES.getTipoCambio())));
        
        return sb.toString();
    }
    
    /**
     * Genera un informe de puntos de los clientes.
     * 
     * @return String con el informe de puntos
     */
    public String generarInformePuntosClientes() {
        if (clientes.isEmpty()) {
            return "No hay clientes registrados.";
        }
        
        // Filtrar clientes con puntos y ordenar por puntos (de mayor a menor)
        List<Cliente> clientesConPuntos = clientes.stream()
            .filter(c -> c.getPuntos() > 0)
            .sorted((c1, c2) -> Integer.compare(c2.getPuntos(), c1.getPuntos()))
            .collect(Collectors.toList());
        
        if (clientesConPuntos.isEmpty()) {
            return "Ningún cliente ha acumulado puntos aún.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("INFORME DE PUNTOS DE CLIENTES\n");
        sb.append("========================================\n");
        
        for (Cliente cliente : clientesConPuntos) {
            sb.append(String.format("%s (Cédula: %d): %,d puntos%n", 
                cliente.getNombre(), 
                cliente.getCedula(),
                cliente.getPuntos()));
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("%s (Cédula Jurídica: %d)\nClientes: %d, Productos: %d, Facturas: %d",
            nombreEmpresa, cedulaJuridica, clientes.size(), productos.size(), facturas.size());
    }
}
