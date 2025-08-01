package com.facturacion.service.impl;

import com.facturacion.model.Cliente;
import com.facturacion.model.Factura;
import com.facturacion.model.LineaDetalle;
import com.facturacion.model.Pago;
import com.facturacion.model.Producto;
import com.facturacion.model.Tarjeta;
import com.facturacion.model.TipoCliente;
import com.facturacion.model.TipoPago;
import com.facturacion.repository.FacturaRepository;
import com.facturacion.service.ClienteService;
import com.facturacion.service.FacturaService;
import com.facturacion.service.ProductoService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de gestión de facturas.
 */
public class FacturaServiceImpl implements FacturaService {
    
    private final FacturaRepository facturaRepository;
    private final ClienteService clienteService;
    private final ProductoService productoService;
    
    /**
     * Constructor que recibe las dependencias necesarias.
     * 
     * @param facturaRepository Repositorio de facturas
     * @param clienteService Servicio de clientes
     * @param productoService Servicio de productos
     * @throws IllegalArgumentException Si algún parámetro es nulo
     */
    public FacturaServiceImpl(FacturaRepository facturaRepository, 
                             ClienteService clienteService,
                             ProductoService productoService) {
        if (facturaRepository == null || clienteService == null || productoService == null) {
            throw new IllegalArgumentException("Los parámetros no pueden ser nulos");
        }
        this.facturaRepository = facturaRepository;
        this.clienteService = clienteService;
        this.productoService = productoService;
    }
    
    @Override
    public Factura crearFactura(Factura factura) {
        if (factura == null) {
            throw new IllegalArgumentException("La factura no puede ser nula");
        }
        
        // Validar la factura
        validarFactura(factura);
        
        // Verificar que el cliente existe
        Cliente cliente = factura.getCliente();
        Cliente clienteExistente = clienteService.buscarPorCedula(cliente.getCedula())
            .orElseThrow(() -> new IllegalArgumentException("El cliente no está registrado"));
            
        // Validar que el tipo de cliente coincida
        if (cliente.getTipo() != clienteExistente.getTipo()) {
            throw new IllegalArgumentException("El tipo de cliente no coincide con el registrado");
        }
        
        // Crear una nueva factura con el cliente existente para asegurar datos actualizados
        Factura nuevaFactura = new Factura(clienteExistente);
        
        // Copiar las líneas de detalle de la factura original a la nueva
        for (LineaDetalle linea : factura.getLineasDetalle()) {
            // Buscar el producto en el sistema para asegurar que tenemos la versión actualizada
            Producto producto = productoService.buscarPorCodigo(linea.getProducto().getCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + linea.getProducto().getCodigo()));
                
            try {
                // Crear y agregar la línea de detalle directamente a la factura
                LineaDetalle nuevaLinea = new LineaDetalle(
                    nuevaFactura.getLineasDetalle().size() + 1, // número de línea
                    producto,
                    linea.getCantidadProducto()
                );
                
                // Agregar la línea de detalle a la factura
                nuevaFactura.getLineasDetalle().add(nuevaLinea);
                
                // Calcular los totales de la factura
                nuevaFactura.calcularTotal();
                
                // Actualizar el inventario del producto usando el servicio
                if (!productoService.actualizarInventario(producto.getCodigo(), -linea.getCantidadProducto())) {
                    throw new IllegalStateException("No hay suficiente inventario para el producto: " + producto.getNombre());
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Error al agregar el producto a la factura: " + e.getMessage(), e);
            }
        }
        
        // El descuento ya se aplica en el constructor de Factura si el cliente es ciudadano de oro
        
        // Reemplazar la factura original con la nueva
        factura = nuevaFactura;
        
        // Verificar el inventario de los productos
        for (LineaDetalle linea : factura.getLineasDetalle()) {
            Producto producto = linea.getProducto();
            if (!productoService.tieneInventarioSuficiente(producto.getCodigo(), linea.getCantidadProducto())) {
                throw new IllegalStateException("No hay suficiente inventario para el producto: " + producto.getNombre());
            }
        }
        
        // Actualizar el inventario
        for (LineaDetalle linea : factura.getLineasDetalle()) {
            Producto producto = linea.getProducto();
            if (!productoService.actualizarInventario(producto.getCodigo(), -linea.getCantidadProducto())) {
                throw new IllegalStateException("Error al actualizar el inventario del producto: " + producto.getNombre());
            }
        }
        
        // Actualizar puntos del cliente si aplica
        if (clienteExistente.isCiudadanoOro() && factura.getTotalPuntos() > 0) {
            clienteService.actualizarPuntos(clienteExistente.getCedula(), factura.getTotalPuntos());
        }
        
        // Guardar la factura
        return facturaRepository.guardar(factura);
    }
    
    @Override
    public Optional<Factura> buscarPorNumero(int numeroFactura) {
        if (numeroFactura <= 0) {
            return Optional.empty();
        }
        return facturaRepository.buscarPorNumero(numeroFactura);
    }
    
    @Override
    public List<Factura> listarTodas() {
        return facturaRepository.buscarTodas();
    }
    
    @Override
    public List<Factura> buscarPorCliente(long cedulaCliente) {
        if (cedulaCliente <= 0) {
            throw new IllegalArgumentException("La cédula debe ser un número positivo");
        }
        return facturaRepository.buscarPorCliente(cedulaCliente);
    }
    
    @Override
    public List<Factura> buscarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }
        
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        
        return facturaRepository.buscarPorRangoFechas(fechaInicio, fechaFin);
    }
    
    @Override
    public boolean procesarPago(int numeroFactura, Pago pago) {
        if (pago == null) {
            throw new IllegalArgumentException("El pago no puede ser nulo");
        }
        
        // Validar moneda
        if (pago.getMoneda() == null) {
            throw new IllegalArgumentException("La moneda de pago es requerida");
        }
        
        return buscarPorNumero(numeroFactura)
            .map(factura -> {
                // Validar que la factura no esté anulada
                if (factura.isAnulada()) {
                    throw new IllegalStateException("No se puede procesar el pago de una factura anulada");
                }
                
                // Validar que la factura no esté ya pagada
                if (factura.isPagada()) {
                    throw new IllegalStateException("La factura ya tiene un pago registrado");
                }
                
                // Registrar el pago usando el método del modelo
                boolean pagoExitoso = factura.registrarPago(pago);
                
                // Si el pago fue exitoso, actualizar puntos del cliente si aplica
                if (pagoExitoso && 
                    factura.getCliente().getTipo() == TipoCliente.OCASIONALES && 
                    factura.getTotalPuntos() > 0) {
                    
                    clienteService.actualizarPuntos(
                        factura.getCliente().getCedula(),
                        factura.getTotalPuntos()
                    );
                }
                
                // Actualizar la factura en el repositorio
                return pagoExitoso && facturaRepository.guardar(factura) != null;
            })
            .orElse(false);
    }
    
    @Override
    public boolean anularFactura(int numeroFactura, String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo de la anulación no puede estar vacío");
        }
        
        return facturaRepository.buscarPorNumero(numeroFactura)
            .map(factura -> {
                try {
                    // Validar que la factura no esté ya anulada
                    if (factura.isAnulada()) {
                        return false; // Ya está anulada
                    }
                    
                    // Validar que la factura no esté pagada
                    if (factura.isPagada()) {
                        throw new IllegalStateException("No se puede anular una factura ya pagada");
                    }
                    
                    // Devolver el inventario de productos
                    for (LineaDetalle linea : factura.getLineasDetalle()) {
                        Producto producto = linea.getProducto();
                        productoService.actualizarInventario(producto.getCodigo(), linea.getCantidadProducto());
                    }
                    
                    // Actualizar los puntos del cliente si aplica
                    if (factura.getCliente().getTipo() == TipoCliente.OCASIONALES && 
                        factura.getCliente().isCiudadanoOro() && 
                        factura.getTotalPuntos() > 0) {
                        
                        clienteService.actualizarPuntos(
                            factura.getCliente().getCedula(), 
                            -factura.getTotalPuntos()
                        );
                    }
                    
                    // Anular la factura usando el método del modelo
                    factura.anular(motivo);
                    return facturaRepository.guardar(factura) != null;
                    
                } catch (IllegalStateException e) {
                    return false; // No se pudo anular
                }
            })
            .orElse(false); // No se encontró la factura
    }
    
    @Override
    public double obtenerTotalVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        return buscarPorRangoFechas(fechaInicio, fechaFin).stream()
            .filter(factura -> !factura.isAnulada())
            .mapToDouble(Factura::calcularTotal)
            .sum();
    }
    
    @Override
    public double obtenerTotalImpuestos(LocalDate fechaInicio, LocalDate fechaFin) {
        return buscarPorRangoFechas(fechaInicio, fechaFin).stream()
            .filter(factura -> !factura.isAnulada())
            .mapToDouble(Factura::calcularTotalImpuestos)
            .sum();
    }
    
    @Override
    public double obtenerTotalDescuentos(LocalDate fechaInicio, LocalDate fechaFin) {
        return buscarPorRangoFechas(fechaInicio, fechaFin).stream()
            .filter(factura -> !factura.isAnulada())
            .mapToDouble(Factura::calcularTotalDescuentos)
            .sum();
    }
    
    @Override
    public String generarReporteVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Factura> facturas = buscarPorRangoFechas(fechaInicio, fechaFin);
        
        StringBuilder reporte = new StringBuilder();
        reporte.append("REPORTE DE VENTAS\n");
        reporte.append("=================\n\n");
        reporte.append(String.format("Período: %s a %s\n\n", fechaInicio, fechaFin));
        
        // Resumen
        double totalVentas = obtenerTotalVentas(fechaInicio, fechaFin);
        double totalImpuestos = obtenerTotalImpuestos(fechaInicio, fechaFin);
        double totalDescuentos = obtenerTotalDescuentos(fechaInicio, fechaFin);
        
        reporte.append("RESUMEN\n");
        reporte.append("-------\n");
        reporte.append(String.format("Total de facturas: %d\n", facturas.size()));
        reporte.append(String.format("Total de ventas: %.2f\n", totalVentas));
        reporte.append(String.format("Total de impuestos: %.2f\n", totalImpuestos));
        reporte.append(String.format("Total de descuentos: %.2f\n\n", totalDescuentos));
        
        // Detalle de facturas
        reporte.append("DETALLE DE FACTURAS\n");
        reporte.append("------------------\n");
        
        for (Factura factura : facturas) {
            String estado = factura.isAnulada() ? "ANULADA" : 
                          (factura.getPago() != null ? "PAGADA" : "PENDIENTE");
            
            reporte.append(String.format("Factura #%d - %s - %s - %s - %.2f\n",
                factura.getNumeroFactura(),
                factura.getFechaHora().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                factura.getCliente().getNombre(),
                estado,
                factura.calcularTotal()
            ));
        }
        
        return reporte.toString();
    }
    
    /**
     * Valida que los datos de la factura sean correctos.
     * 
     * @param factura Factura a validar
     * @throws IllegalArgumentException Si algún dato es inválido
     */
    private void validarFactura(Factura factura) {
        if (factura.getCliente() == null) {
            throw new IllegalArgumentException("La factura debe tener un cliente asociado");
        }
        
        if (factura.getLineasDetalle() == null || factura.getLineasDetalle().isEmpty()) {
            throw new IllegalArgumentException("La factura debe tener al menos una línea de detalle");
        }
        
        // Validar que no haya productos duplicados
        long productosUnicos = factura.getLineasDetalle().stream()
            .map(linea -> linea.getProducto().getCodigo())
            .distinct()
            .count();
            
        if (productosUnicos < factura.getLineasDetalle().size()) {
            throw new IllegalArgumentException("No se pueden incluir productos duplicados en la factura");
        }
        
        // Validar cantidades positivas
        for (LineaDetalle linea : factura.getLineasDetalle()) {
            if (linea.getCantidadProducto() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
            }
        }
    }
}
