package com.facturacion.test;

import com.facturacion.model.*;
import com.facturacion.repository.ClienteRepository;
import com.facturacion.repository.FacturaRepository;
import com.facturacion.repository.ProductoRepository;
import com.facturacion.repository.impl.ClienteRepositoryImpl;
import com.facturacion.repository.impl.FacturaRepositoryImpl;
import com.facturacion.repository.impl.ProductoRepositoryImpl;
import com.facturacion.service.ClienteService;
import com.facturacion.service.FacturaService;
import com.facturacion.service.ProductoService;
import com.facturacion.service.impl.ClienteServiceImpl;
import com.facturacion.service.impl.FacturaServiceImpl;
import com.facturacion.service.impl.ProductoServiceImpl;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Clase de prueba de integración para validar el funcionamiento conjunto de los servicios.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FacturacionIntegrationTest {
    
    private static ClienteService clienteService;
    private static ProductoService productoService;
    private static FacturaService facturaService;
    
    private static Cliente clienteOcasional;
    private static Cliente clienteCorporativo;
    private static Producto productoPerecedero;
    private static Producto productoNoPerecedero;
    
    @BeforeAll
    public static void setUp() {
        // Inicializar repositorios
        ClienteRepository clienteRepository = new ClienteRepositoryImpl();
        ProductoRepository productoRepository = new ProductoRepositoryImpl();
        FacturaRepository facturaRepository = new FacturaRepositoryImpl();
        
        // Inicializar servicios
        clienteService = new ClienteServiceImpl(clienteRepository);
        productoService = new ProductoServiceImpl(productoRepository);
        facturaService = new FacturaServiceImpl(facturaRepository, clienteService, productoService);
        
        // Configurar datos de prueba
        configurarDatosPrueba();
    }
    
    private static void configurarDatosPrueba() {
        // Crear cliente ocasional (Ciudadano de Oro)
        clienteOcasional = new Cliente(
            101110111, 
            "Juan Pérez", 
            "juan@example.com", 
            "1234-5678", 
            "Calle 123", 
            Cliente.TipoCliente.OCASIONAL,
            ""
        );
        clienteOcasional.setCiudadanoOro(true);
        
        // Crear cliente corporativo
        clienteCorporativo = new Cliente(
            202220222, 
            "Empresa XYZ", 
            "contacto@empresa.com", 
            "2233-4455", 
            "Avenida Principal", 
            Cliente.TipoCliente.CORPORATIVO,
            "María González"
        );
        
        // Crear producto perecedero
        productoPerecedero = new ProductoPerecedero(
            "Leche Entera 1L", 
            "Lácteo envasado en cartón", 
            1200.0, 
            50, 
            new Impuesto("IVA", 13.0),
            LocalDate.now().plusDays(30)
        );
        
        // Crear producto no perecedero
        productoNoPerecedero = new Producto(
            "Arroz 1kg", 
            "Arroz grano entero", 
            1500.0, 
            100, 
            new Impuesto("IVA", 13.0)
        );
    }
    
    @Test
    @Order(1)
    public void testRegistrarClientes() {
        // Registrar clientes
        Cliente cliente1 = clienteService.registrarCliente(clienteOcasional);
        Cliente cliente2 = clienteService.registrarCliente(clienteCorporativo);
        
        assertNotNull(cliente1);
        assertNotNull(cliente2);
        assertEquals("Juan Pérez", cliente1.getNombre());
        assertEquals("Empresa XYZ", cliente2.getNombre());
        assertTrue(cliente1.isCiudadanoOro());
    }
    
    @Test
    @Order(2)
    public void testRegistrarProductos() {
        // Registrar productos
        Producto producto1 = productoService.registrarProducto(productoPerecedero);
        Producto producto2 = productoService.registrarProducto(productoNoPerecedero);
        
        assertNotNull(producto1);
        assertNotNull(producto2);
        assertEquals("Leche Entera 1L", producto1.getNombre());
        assertEquals("Arroz 1kg", producto2.getNombre());
        assertTrue(producto1 instanceof ProductoPerecedero);
    }
    
    @Test
    @Order(3)
    public void testCrearFacturaConDescuento() {
        // Buscar cliente y productos
        Cliente cliente = clienteService.buscarPorCedula(101110111).orElseThrow();
        Producto leche = productoService.buscarPorNombre("Leche").get(0);
        Producto arroz = productoService.buscarPorNombre("Arroz").get(0);
        
        // Crear factura
        Factura factura = new Factura(cliente);
        
        // Agregar productos a la factura
        factura.agregarProducto(leche, 2);
        factura.agregarProducto(arroz, 1);
        
        // Registrar factura
        Factura facturaCreada = facturaService.crearFactura(factura);
        
        // Verificar que se aplicó el descuento del 10% al cliente Ciudadano de Oro
        double subtotal = (leche.getPrecio() * 2 + arroz.getPrecio());
        double descuentoEsperado = subtotal * 0.10;
        double totalEsperado = (subtotal + (subtotal * 0.13)) - descuentoEsperado; // 13% IVA
        
        assertNotNull(facturaCreada);
        assertEquals(2, facturaCreada.getLineasDetalle().size());
        assertEquals(descuentoEsperado, facturaCreada.calcularTotalDescuentos(), 0.01);
        assertEquals(totalEsperado, facturaCreada.calcularTotal(), 0.01);
    }
    
    @Test
    @Order(4)
    public void testProcesarPago() {
        // Buscar factura
        List<Factura> facturas = facturaService.buscarPorCliente(101110111);
        assertFalse(facturas.isEmpty());
        
        Factura factura = facturas.get(0);
        
        // Procesar pago en efectivo
        Pago pago = new Pago(
            "EFECTIVO",
            factura.calcularTotal(),
            factura.calcularTotal() + 1000, // Paga con 1000 de más
            "CRC",
            null, null, null, null
        );
        
        boolean pagoProcesado = facturaService.procesarPago(factura.getNumeroFactura(), pago);
        assertTrue(pagoProcesado);
        
        // Verificar que la factura está pagada
        Factura facturaActualizada = facturaService.buscarPorNumero(factura.getNumeroFactura()).orElseThrow();
        assertTrue(facturaActualizada.isPagada());
        assertEquals(1000.0, facturaActualizada.getVuelto(), 0.01);
    }
    
    @Test
    @Order(5)
    public void testGenerarReporte() {
        // Generar reporte de ventas del día actual
        LocalDate hoy = LocalDate.now();
        String reporte = facturaService.generarReporteVentas(hoy, hoy);
        
        assertNotNull(reporte);
        assertTrue(reporte.contains("REPORTE DE VENTAS"));
        assertTrue(reporte.contains("Juan Pérez"));
    }
}
