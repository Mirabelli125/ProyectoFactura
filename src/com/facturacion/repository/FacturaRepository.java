package com.facturacion.repository;

import com.facturacion.model.Factura;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz para el repositorio de facturas.
 * Define las operaciones CRUD básicas para la entidad Factura.
 */
public interface FacturaRepository {
    
    /**
     * Guarda una factura en el repositorio.
     * 
     * @param factura Factura a guardar
     * @return La factura guardada, o null si no se pudo guardar
     */
    Factura guardar(Factura factura);
    
    /**
     * Busca una factura por su número.
     * 
     * @param numeroFactura Número de factura a buscar
     * @return Un Optional con la factura si se encuentra, o vacío si no
     */
    Optional<Factura> buscarPorNumero(int numeroFactura);
    
    /**
     * Busca todas las facturas en el repositorio.
     * 
     * @return Lista de todas las facturas
     */
    List<Factura> buscarTodas();
    
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
     * Busca facturas dentro de un rango de fechas (inclusive).
     * 
     * @param fechaInicio Fecha de inicio (se ignorará la hora)
     * @param fechaFin Fecha de fin (se ignorará la hora)
     * @return Lista de facturas dentro del rango de fechas
     */
    List<Factura> buscarPorRangoFechas(Date fechaInicio, Date fechaFin);
    
    /**
     * Busca las facturas del día actual.
     * 
     * @return Lista de facturas del día actual
     */
    List<Factura> buscarDelDia();
    
    /**
     * Obtiene la última factura ingresada.
     * 
     * @return La última factura, o vacío si no hay facturas
     */
    Optional<Factura> obtenerUltimaFactura();
    
    /**
     * Verifica si existe una factura con el número especificado.
     * 
     * @param numeroFactura Número de factura a verificar
     * @return true si existe una factura con ese número, false en caso contrario
     */
    boolean existePorNumero(int numeroFactura);
}
