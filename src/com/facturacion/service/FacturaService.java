package com.facturacion.service;

import com.facturacion.model.Factura;
import com.facturacion.model.Pago;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz para el servicio de gestión de facturas.
 * Define las operaciones de negocio relacionadas con las facturas.
 */
public interface FacturaService {
    
    /**
     * Crea una nueva factura en el sistema.
     * 
     * @param factura Factura a crear
     * @return La factura creada con su número asignado
     * @throws IllegalArgumentException Si la factura es nula o inválida
     */
    Factura crearFactura(Factura factura);
    
    /**
     * Busca una factura por su número.
     * 
     * @param numeroFactura Número de factura a buscar
     * @return Un Optional con la factura si se encuentra, o vacío si no
     */
    Optional<Factura> buscarPorNumero(int numeroFactura);
    
    /**
     * Busca todas las facturas registradas.
     * 
     * @return Lista de todas las facturas
     */
    List<Factura> listarTodas();
    
    /**
     * Busca facturas por cliente.
     * 
     * @param cedulaCliente Cédula del cliente
     * @return Lista de facturas del cliente
     */
    List<Factura> buscarPorCliente(long cedulaCliente);
    
    /**
     * Busca facturas por rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio (inclusive)
     * @param fechaFin Fecha de fin (inclusive)
     * @return Lista de facturas en el rango de fechas
     */
    /**
     * Busca facturas por rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio (inclusive)
     * @param fechaFin Fecha de fin (inclusive)
     * @return Lista de facturas en el rango de fechas
     */
    List<Factura> buscarPorRangoFechas(Date fechaInicio, Date fechaFin);
    
    /**
     * Busca las facturas del día actual.
     * 
     * @return Lista de facturas del día actual
     */
    List<Factura> buscarDelDia();
    
    /**
     * Procesa el pago de una factura.
     * 
     * @param numeroFactura Número de la factura a pagar
     * @param pago Datos del pago
     * @return true si el pago se procesó correctamente, false en caso contrario
     */
    boolean procesarPago(int numeroFactura, Pago pago);
    
    /**
     * Anula una factura existente.
     * 
     * @param numeroFactura Número de la factura a anular
     * @param motivo Motivo de la anulación
     * @return true si se anuló correctamente, false en caso contrario
     */
    boolean anularFactura(int numeroFactura, String motivo);
    
    /**
     * Obtiene el total de ventas en un rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio (inclusive)
     * @param fechaFin Fecha de fin (inclusive)
     * @return Total de ventas en el rango de fechas
     */
    /**
     * Obtiene el total de ventas en un rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio (inclusive)
     * @param fechaFin Fecha de fin (inclusive)
     * @return Total de ventas en el rango de fechas
     */
    double obtenerTotalVentas(Date fechaInicio, Date fechaFin);
    
    /**
     * Obtiene el total de impuestos recaudados en un rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio (inclusive)
     * @param fechaFin Fecha de fin (inclusive)
     * @return Total de impuestos recaudados en el rango de fechas
     */
    double obtenerTotalImpuestos(Date fechaInicio, Date fechaFin);
    
    /**
     * Obtiene el total de descuentos aplicados en un rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio (inclusive)
     * @param fechaFin Fecha de fin (inclusive)
     * @return Total de descuentos aplicados en el rango de fechas
     */
    double obtenerTotalDescuentos(Date fechaInicio, Date fechaFin);
    
    /**
     * Genera un reporte de ventas en formato de texto.
     * 
     * @param fechaInicio Fecha de inicio (inclusive)
     * @param fechaFin Fecha de fin (inclusive)
     * @return Reporte de ventas en formato de texto
     */
    String generarReporteVentas(Date fechaInicio, Date fechaFin);
}
