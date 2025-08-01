package com.facturacion;

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
import com.facturacion.ui.MainWindow;
import javax.swing.SwingUtilities;

/**
 * Clase principal de la aplicación de facturación.
 * Inicializa los componentes principales y muestra la ventana principal.
 */
public class App {
    
    // Repositorios
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final FacturaRepository facturaRepository;
    
    // Servicios
    private final ClienteService clienteService;
    private final ProductoService productoService;
    private final FacturaService facturaService;
    
    // Interfaz de usuario
    private MainWindow mainWindow;
    
    /**
     * Constructor de la aplicación.
     * Inicializa los repositorios y servicios.
     */
    public App() {
        // Inicializar repositorios
        this.clienteRepository = new ClienteRepositoryImpl();
        this.productoRepository = new ProductoRepositoryImpl();
        this.facturaRepository = new FacturaRepositoryImpl();
        
        // Inicializar servicios
        this.clienteService = new ClienteServiceImpl(clienteRepository);
        this.productoService = new ProductoServiceImpl(productoRepository);
        this.facturaService = new FacturaServiceImpl(
            facturaRepository, 
            clienteService,
            productoService
        );
        
        // Inicializar la interfaz de usuario en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            mainWindow = new MainWindow(this);
            mainWindow.setVisible(true);
        });
    }
    
    /**
     * Punto de entrada de la aplicación.
     * 
     * @param args Argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        // Configurar el aspecto visual del sistema operativo
        try {
            javax.swing.UIManager.setLookAndFeel(
                javax.swing.UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception ex) {
            // Si hay un error, usar el aspecto por defecto
            ex.printStackTrace();
        }
        
        // Iniciar la aplicación
        new App();
    }
    
    // Getters para los servicios
    
    public ClienteService getClienteService() {
        return clienteService;
    }
    
    public ProductoService getProductoService() {
        return productoService;
    }
    
    public FacturaService getFacturaService() {
        return facturaService;
    }
}
