package com.facturacion.ui;

import com.facturacion.App;
import java.awt.Dimension;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Clase base para todos los paneles de la aplicación.
 * Proporciona funcionalidades comunes y referencias a la aplicación principal.
 */
public abstract class BasePanel extends JPanel {
    
    protected final App app;
    
    /**
     * Crea una nueva instancia del panel base.
     * 
     * @param app Referencia a la aplicación principal
     */
    public BasePanel(App app) {
        this.app = app;
        initComponents();
        setupPanel();
    }
    
    /**
     * Inicializa los componentes del panel.
     * Debe ser implementado por las clases hijas.
     */
    protected abstract void initComponents();
    
    /**
     * Configura las propiedades del panel.
     * Puede ser sobrescrito por las clases hijas para personalizar el comportamiento.
     */
    protected void setupPanel() {
        setLayout(null);
        setPreferredSize(new Dimension(1000, 700));
    }
    
    /**
     * Muestra un mensaje de error en un diálogo.
     * 
     * @param mensaje Mensaje de error a mostrar
     */
    protected void mostrarError(String mensaje) {
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
    protected void mostrarInformacion(String mensaje) {
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
    protected boolean confirmar(String mensaje) {
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
