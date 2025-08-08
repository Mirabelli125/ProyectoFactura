package com.facturacion.ui;

import com.facturacion.App;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

/**
 * Ventana principal de la aplicación de facturación.
 * Contiene los diferentes paneles de la aplicación en pestañas.
 */
public class MainWindow extends JFrame {
    
    private final App app;
    // Constantes
    private static final String APP_TITLE = "Sistema de Facturación";
    private static final String CONFIRM_EXIT_MSG = "¿Está seguro que desea salir de la aplicación?";
    private static final String CONFIRM_EXIT_TITLE = "Confirmar salida";
    private static final String SAVE_SUCCESS_MSG = "Datos guardados correctamente.";
    private static final String SAVE_ERROR_MSG = "Error al guardar los datos: ";
    private static final String INFO_TITLE = "Información";
    private static final String ERROR_TITLE = "Error";
    
    // Componentes de la interfaz
    private JTabbedPane tabbedPane;
    private JMenuBar menuBar;
    private JPanel statusBar;
    
    // Paneles de la aplicación
    private ProductoPanel productoPanel;
    private ClientePanel clientePanel;
    private JPanel facturacionPanel; // Placeholder for future implementation
    private JPanel reportesPanel; // Placeholder for future implementation
    
    /**
     * Crea una nueva instancia de la ventana principal.
     * 
     * @param app Referencia a la aplicación principal
     */
    public MainWindow(App app) {
        this.app = app;
        initComponents();
        setupWindow();
    }
    
    /**
     * Inicializa los componentes de la interfaz de usuario.
     */
    /**
     * Inicializa los componentes de la interfaz de usuario.
     */
    private void initComponents() {
        setTitle(APP_TITLE);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 768));
        
        // Configurar el cierre de la aplicación
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmarSalida();
            }
        });
        
        // Configurar la barra de menú
        setupMenuBar();
        
        // Crear el panel de pestañas
        tabbedPane = new JTabbedPane();
        
        // Crear e inicializar los paneles
        try {
            productoPanel = new ProductoPanel(app);
            clientePanel = new ClientePanel(app);
            
            // Initialize placeholder panels for future implementation
            facturacionPanel = new JPanel();
            facturacionPanel.add(new JLabel("Módulo de Facturación (Próximamente)"));
            
            reportesPanel = new JPanel();
            reportesPanel.add(new JLabel("Módulo de Reportes (Próximamente)"));
            
            // Agregar los paneles a las pestañas
            tabbedPane.addTab("Productos", null, productoPanel, "Mantenimiento de productos");
            tabbedPane.addTab("Clientes", null, clientePanel, "Registro y consulta de clientes");
            tabbedPane.addTab("Facturación", null, facturacionPanel, "Proceso de facturación");
            tabbedPane.addTab("Reportes", null, reportesPanel, "Reportes y estadísticas");
            
        } catch (Exception e) {
            mostrarError("Error al inicializar los paneles: " + e.getMessage());
            // Intentar continuar con los paneles que se pudieron cargar
            if (tabbedPane.getTabCount() == 0) {
                JOptionPane.showMessageDialog(this, 
                    "No se pudo cargar ningún panel. La aplicación se cerrará.",
                    "Error crítico",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }
        
        // Configurar la barra de estado
        setupStatusBar();
        
        // Configurar el diseño de la ventana
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
        
        // Centrar la ventana en la pantalla
        setLocationRelativeTo(null);
    }
    
    /**
     * Configura la barra de menú de la aplicación.
     */
    private void setupMenuBar() {
        menuBar = new JMenuBar();
        
        // Menú Archivo
        JMenu fileMenu = new JMenu("Archivo");
        fileMenu.setMnemonic(KeyEvent.VK_A);
        
        // Opción Guardar
        JMenuItem saveItem = new JMenuItem("Guardar", KeyEvent.VK_G);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        saveItem.addActionListener(e -> guardarDatos());
        
        // Opción Salir
        JMenuItem exitItem = new JMenuItem("Salir", KeyEvent.VK_S);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        exitItem.addActionListener(e -> confirmarSalida());
        
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Menú Ayuda
        JMenu helpMenu = new JMenu("Ayuda");
        helpMenu.setMnemonic(KeyEvent.VK_Y);
        
        // Opción Acerca de
        JMenuItem aboutItem = new JMenuItem("Acerca de...");
        aboutItem.addActionListener(e -> mostrarAcercaDe());
        helpMenu.add(aboutItem);
        
        // Agregar menús a la barra
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        
        // Establecer la barra de menú
        setJMenuBar(menuBar);
    }
    
    /**
     * Configura la barra de estado de la aplicación.
     */
    private void setupStatusBar() {
        statusBar = new JPanel();
        statusBar.setLayout(new BorderLayout());
        statusBar.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        
        JLabel statusLabel = new JLabel(" Listo");
        statusBar.add(statusLabel, BorderLayout.WEST);
    }
    
    /**
     * Configura las propiedades de la ventana.
     */
    private void setupWindow() {
        // Establecer el ícono de la aplicación
        try {
            // Cargar el ícono desde los recursos
            // setIconImage(new ImageIcon(getClass().getResource("/com/facturacion/images/icon.png")).getImage());
        } catch (Exception e) {
            // Si no se puede cargar el ícono, continuar sin él
            System.err.println("No se pudo cargar el ícono de la aplicación: " + e.getMessage());
        }
        
        // Configurar el comportamiento al cerrar
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }
    
    /**
     * Muestra el diálogo "Acerca de".
     */
    private void mostrarAcercaDe() {
        String mensaje = "Sistema de Facturación\n" +
                       "Versión 1.0\n" +
                       "© 2023 - Todos los derechos reservados\n\n" +
                       "Desarrollado por el equipo de desarrollo.";
        
        JOptionPane.showMessageDialog(
            this,
            mensaje,
            "Acerca de",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Muestra un diálogo de confirmación al intentar salir de la aplicación.
     */
    private void confirmarSalida() {
        // Verificar si hay cambios sin guardar
        if (hayCambiosSinGuardar()) {
            int opcion = JOptionPane.showConfirmDialog(
                this,
                "Hay cambios sin guardar. ¿Desea guardar antes de salir?",
                "Cambios sin guardar",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (opcion == JOptionPane.CANCEL_OPTION) {
                return; // Cancelar la salida
            } else if (opcion == JOptionPane.YES_OPTION) {
                if (!guardarDatos()) {
                    // Si hay un error al guardar, no salir
                    return;
                }
            }
        } else {
            // Preguntar confirmación para salir
            int opcion = JOptionPane.showConfirmDialog(
                this,
                CONFIRM_EXIT_MSG,
                CONFIRM_EXIT_TITLE,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (opcion != JOptionPane.YES_OPTION) {
                return; // Cancelar la salida
            }
        }
        
        // Cerrar la aplicación
        System.exit(0);
    }
    
    /**
     * Verifica si hay cambios sin guardar en la aplicación.
     * 
     * @return true si hay cambios sin guardar, false en caso contrario
     */
    private boolean hayCambiosSinGuardar() {
        // Implementar lógica para verificar cambios sin guardar
        // Por ejemplo, revisar si hay facturas en proceso o cambios en los paneles
        return false; // Por defecto, asumir que no hay cambios
    }
    
    /**
     * Guarda los datos de la aplicación.
     * 
     * @return true si se guardaron los datos correctamente, false en caso de error
     */
    private boolean guardarDatos() {
        try {
            // Aquí se podrían guardar los datos pendientes si fuera necesario
            // Por ahora, los repositorios guardan automáticamente los cambios
            
            // Forzar la sincronización de los datos
            // Nota: Se asume que los paneles implementan la lógica de guardado internamente
            // a través de sus respectivos servicios
            
            JOptionPane.showMessageDialog(
                this,
                SAVE_SUCCESS_MSG,
                INFO_TITLE,
                JOptionPane.INFORMATION_MESSAGE
            );
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                SAVE_ERROR_MSG + e.getMessage(),
                ERROR_TITLE,
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }
    
    /**
     * Muestra un mensaje de error en un diálogo.
     * 
     * @param mensaje Mensaje de error a mostrar
     */
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
            this,
            mensaje,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Muestra un mensaje de información en un diálogo.
     * 
     * @param mensaje Mensaje de información a mostrar
     */
    public void mostrarInformacion(String mensaje) {
        JOptionPane.showMessageDialog(
            this,
            mensaje,
            "Información",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Muestra un diálogo de confirmación.
     * 
     * @param mensaje Mensaje a mostrar
     * @return true si el usuario confirma, false en caso contrario
     */
    public boolean confirmar(String mensaje) {
        int opcion = JOptionPane.showConfirmDialog(
            this,
            mensaje,
            "Confirmar",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        return opcion == JOptionPane.YES_OPTION;
    }
}
