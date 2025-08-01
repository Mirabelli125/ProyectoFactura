package com.facturacion.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

/**
 * Clase que representa una factura en el sistema.
 * Contiene la información del cliente, líneas de detalle, pagos y cálculos financieros.
 */
public class Factura {
    private static int contadorFactura = 1;
    
    private final int numeroFactura;
    private final Date fechaFactura;
    private Cliente cliente;
    private final List<LineaDetalle> lineasDetalle;
    private Pago pago;
    private double impuesto;
    private double descuento;
    private double subtotal;
    private double total;
    private boolean cerrada;
    private String motivoAnulacion;
    private Date fechaHoraPago;
    private Date fechaHoraAnulacion;

    /**
     * Constructor para crear una nueva factura.
     * 
     * @param cliente Cliente asociado a la factura
     */
    public Factura(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("La factura debe estar asociada a un cliente");
        }
        
        this.numeroFactura = contadorFactura++;
        this.fechaFactura = new Date();
        this.cliente = cliente;
        this.lineasDetalle = new ArrayList<>();
        this.impuesto = 0.0;
        this.descuento = 0.0;
        this.subtotal = 0.0;
        this.total = 0.0;
        
        // Si el cliente es ocasional y ciudadano de oro, aplicar descuento
        if (cliente.getTipo() == TipoCliente.OCASIONALES && cliente.isCiudadanoOro()) {
            this.descuento = 0.10; // 10% de descuento
        }
    }
    
    /**
     * Agrega un producto a la factura con la cantidad especificada.
     * 
     * @param producto Producto a agregar
     * @param cantidad Cantidad del producto
     * @return true si se pudo agregar el producto, false en caso contrario
     */
    /**
    public boolean agregarProducto(Producto producto, int cantidad) {
        if (cerrada) {
            throw new IllegalStateException("No se pueden agregar productos a una factura cerrada");
        }
        
        if (producto == null || cantidad <= 0) {
            return false;
        }
        
        // Verificar si el producto ya está en la factura
        for (LineaDetalle linea : lineasDetalle) {
            if (linea.getProducto().equals(producto)) {
                // Actualizar cantidad en la línea existente
                if (linea.setCantidad(linea.getCantidad() + cantidad)) {
                    calcularTotal();
                    return true;
                }
                return false;
            }
        }
        
        // Crear nueva línea de detalle
        LineaDetalle nuevaLinea = new LineaDetalle(lineasDetalle.size() + 1, producto, cantidad);
        lineasDetalle.add(nuevaLinea);
        calcularTotal();
        return true;
    }
    
    /**
     * Elimina un producto de la factura.
     * 
     * @param numeroLinea Número de línea a eliminar (1-based)
     * @return true si se eliminó el producto, false en caso contrario
     */
    public boolean eliminarProducto(int numeroLinea) {
        if (cerrada) {
            throw new IllegalStateException("No se pueden eliminar productos de una factura cerrada");
        }
        
        // Convertir a 0-based index
        int indice = numeroLinea - 1;
        
        if (indice < 0 || indice >= lineasDetalle.size()) {
            return false;
        }
        
        // No necesitamos actualizar los números de línea ya que LineaDetalle es inmutable
        // en cuanto a su número de línea
        lineasDetalle.remove(indice);
        
        // Recalcular totales
        calcularTotal();
        return true;
    }
    
    /**
     * Registra un pago para la factura.
     * 
     * @param pago Pago a registrar
     * @return true si el pago se registró correctamente, false en caso contrario
     */
    public boolean registrarPago(Pago pago) {
        if (cerrada) {
            throw new IllegalStateException("La factura ya está cerrada");
        }
        
        if (pago == null) {
            return false;
        }
        
        // Verificar que el monto del pago cubra el total
        double montoPago = pago.getMontoEnColones();
        if (montoPago < total) {
            return false; // Pago insuficiente
        }
        
        this.pago = pago;
        this.cerrada = true;
        this.fechaHoraPago = new Date();
        
        return true;
    }
    
    /**
     * Actualiza los totales de la factura (subtotal, impuesto, descuento y total).
     */
    private void actualizarTotales() {
        // Recalcular todos los totales
        calcularTotal();
    }
    
    /**
     * Calcula el total de la factura basado en las líneas de detalle.
     * @return El total calculado de la factura
     */
    public double calcularTotal() {
        // Calcular subtotal e impuestos
        this.subtotal = lineasDetalle.stream()
            .mapToDouble(LineaDetalle::getSubtotal)
            .sum();
            
        this.impuesto = lineasDetalle.stream()
            .mapToDouble(LineaDetalle::getImpuesto)
            .sum();
        
        // Aplicar descuento si el cliente es Ciudadano de Oro
        double montoDescuento = 0;
        if (cliente != null && cliente.isCiudadanoOro()) {
            montoDescuento = this.subtotal * 0.10; // 10% de descuento
            this.descuento = 0.10;
        } else {
            this.descuento = 0.0;
        }
        
        // Calcular total
        this.total = (this.subtotal + this.impuesto) - montoDescuento;
        return this.total;
    }
    
    /**
     * Verifica si la factura está pagada.
     * 
     * @return true si la factura tiene un pago registrado, false en caso contrario
     */
    public boolean isPagada() {
        return pago != null;
    }
    
    /**
     * Verifica si la factura está cerrada.
     * 
     * @return true si la factura está cerrada, false en caso contrario
     */
    public boolean isCerrada() {
        return cerrada;
    }
    
    /**
     * Obtiene el vuelto a devolver al cliente.
     * Solo aplica para pagos en efectivo.
     * 
     * @return Monto del vuelto, o 0 si no aplica
     */
    public double getVuelto() {
        if (pago == null || !pago.esEfectivo()) {
            return 0.0;
        }
        return pago.getMontoEnColones() - total;
    }
    
    /**
     * Obtiene el total de puntos generados por la factura.
     * 
     * @return Total de puntos (1 punto por cada 1000 colones)
     */
    public int getTotalPuntos() {
        return (int)(total / 1000);
    }
    
    /**
     * Obtiene el total de la factura en letras.
     * 
     * @return Total en letras
     */
    public String getTotalEnLetras() {
        // Implementar conversión de número a letras
        // Por ahora devolvemos el número como texto
        return String.format("%.2f", total);
    }
    
    // Getters
    public int getNumeroFactura() {
        return numeroFactura;
    }

    public Date getFechaFactura() {
        return new Date(fechaFactura.getTime());
    }
    
    // Alias for getFechaFactura() to maintain compatibility
    public Date getFechaHora() {
        return getFechaFactura();
    }

    public Cliente getCliente() {
        return cliente;
    }

    public List<LineaDetalle> getLineasDetalle() {
        return new ArrayList<>(lineasDetalle); // Retorna una copia para evitar modificaciones externas
    }

    public Pago getPago() {
        return pago;
    }
    

    
    /**
     * Establece el pago de la factura.
     * 
     * @param pago Pago a establecer
     */
    public void setPago(Pago pago) {
        if (cerrada) {
            throw new IllegalStateException("No se puede modificar el pago de una factura cerrada");
        }
        this.pago = pago;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getImpuesto() {
        return impuesto;
    }

    public double getDescuento() {
        return descuento;
    }

    public double getTotal() {
        return total;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Factura factura = (Factura) o;
        return numeroFactura == factura.numeroFactura;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroFactura);
    }


    
    /**
     * Obtiene el total de impuestos de la factura.
     * @return El total de impuestos
     */
    public double calcularTotalImpuestos() {
        return lineasDetalle.stream()
            .mapToDouble(LineaDetalle::getImpuesto)
            .sum();
    }
    
    /**
     * Obtiene el total de descuentos de la factura.
     * @return El total de descuentos
     */
    public double calcularTotalDescuentos() {
        if (cliente != null && cliente.isCiudadanoOro()) {
            return subtotal * 0.10; // 10% de descuento para clientes Ciudadano de Oro
        }
        return 0.0;
    }
    
    /**
     * Verifica si la factura está anulada.
     * @return true si la factura está anulada, false en caso contrario
     */
    public boolean isAnulada() {
        return fechaHoraAnulacion != null;
    }
    
    /**
     * Anula la factura con un motivo específico.
     * @param motivo Motivo de la anulación
     * @return true si se anuló correctamente, false si ya estaba anulada
     */
    public boolean anular(String motivo) {
        if (isAnulada()) {
            return false;
        }
        
        this.motivoAnulacion = motivo;
        this.fechaHoraAnulacion = new Date();
        return true;
    }
    
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        
        // Encabezado
        sb.append("========================================\n");
        sb.append("FACTURA #").append(String.format("%06d", numeroFactura)).append("\n");
        sb.append("Fecha: ").append(sdf.format(fechaFactura)).append("\n");
        sb.append("Cliente: ").append(cliente.getNombre()).append(" (")
          .append(cliente.getTipo()).append(")\n");
        sb.append("----------------------------------------\n");
        
        // Detalle
        sb.append(String.format("%-4s %-30s %8s %10s %10s\n", 
            "#", "DESCRIPCIÓN", "CANT", "PRECIO", "TOTAL"));
        
        for (LineaDetalle linea : lineasDetalle) {
            sb.append(linea.toString()).append("\n");
        }
        
        // Totales
        sb.append("----------------------------------------\n");
        sb.append(String.format("SUBTOTAL: %35.2f\n", subtotal));
        sb.append(String.format("IMPUESTO: %35.2f\n", impuesto));
        
        if (descuento > 0) {
            sb.append(String.format("DESCUENTO (Ciudadano de Oro): %19.2f\n", 
                subtotal * descuento));
        }
        
        sb.append("----------------------------------------\n");
        sb.append(String.format("TOTAL: %40.2f\n", total));
        
        // Estado de pago
        sb.append("----------------------------------------\n");
        if (isPagada()) {
            sb.append("ESTADO: PAGADA\n");
            sb.append("MÉTODO: ").append(pago.getTipoPago()).append("\n");
            
            if (pago.getTipoPago() == TipoPago.CONTADO) {
                double montoPago = pago.getMonto();
                if (pago.getMoneda() == Moneda.DOLARES) {
                    montoPago *= pago.getTipoCambio();
                }
                double vuelto = montoPago - total;
                if (vuelto > 0) {
                    sb.append(String.format("VUELTO: %36.2f\n", vuelto));
                }
            }
        } else {
            sb.append("ESTADO: PENDIENTE\n");
        }
        
        sb.append("========================================\n");
        return sb.toString();
    }
}
