package com.facturacion.ui;

import com.facturacion.App;
import com.facturacion.model.Cliente;
import com.facturacion.model.TipoCliente;
import com.facturacion.service.ClienteService;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SpinnerNumberModel;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * Panel para el registro y consulta de clientes.
 * Permite listar, agregar, modificar y eliminar clientes.
 */
public class ClientePanel extends BasePanel {
    
    // Componentes de la interfaz
    private JTable tablaClientes;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JButton btnNuevo;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JComboBox<TipoCliente> cmbTipoFiltro;
    
    // Filtro para la tabla
    private TableRowSorter<TableModel> sorter;
    
    // Servicio de clientes
    private final ClienteService clienteService;
    
    /**
     * Crea una nueva instancia del panel de clientes.
     * 
     * @param app Referencia a la aplicación principal
     */
    public ClientePanel(App app) {
        super(app);
        this.clienteService = app.getClienteService();
        initComponents();
        cargarClientes();
    }
    
    @Override
    protected void initComponents() {
        // Configurar el diseño del panel
        setLayout(new BorderLayout(5, 5));
        
        // Panel principal para el contenido
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Panel de búsqueda
        JPanel panelBusqueda = new JPanel(new GridBagLayout());
        panelBusqueda.setBorder(BorderFactory.createTitledBorder("Búsqueda"));
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panelBusqueda.add(new JLabel("Buscar:"), gbc);
        
        txtBuscar = new JTextField(20);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panelBusqueda.add(txtBuscar, gbc);
        
        // Combo para filtrar por tipo de cliente
        cmbTipoFiltro = new JComboBox<>(TipoCliente.values());
        cmbTipoFiltro.insertItemAt(null, 0); // Opción "Todos"
        cmbTipoFiltro.setSelectedIndex(0);
        cmbTipoFiltro.addActionListener(e -> filtrarTabla());
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        panelBusqueda.add(cmbTipoFiltro, gbc);
        
        // Panel de la tabla
        JPanel panelTabla = new JPanel(new GridBagLayout());
        panelTabla.setBorder(javax.swing.BorderFactory.createTitledBorder("Clientes"));
        
        // Crear la tabla de clientes
        modeloTabla = new DefaultTableModel(
            new Object[]{"Cédula", "Nombre", "Tipo", "Contacto", "Ciudadano Oro", "Puntos"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer que la tabla no sea editable directamente
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) { // Columna de Ciudadano Oro
                    return Boolean.class;
                } else if (columnIndex == 5) { // Columna de Puntos
                    return Integer.class;
                }
                return String.class;
            }
        };
        
        tablaClientes = new JTable(modeloTabla);
        sorter = new TableRowSorter<>(modeloTabla);
        tablaClientes.setRowSorter(sorter);
        
        JScrollPane scrollPane = new JScrollPane(tablaClientes);
        scrollPane.setPreferredSize(new Dimension(900, 400));
        
        panelTabla.add(scrollPane, gbc);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new GridBagLayout());
        
        btnNuevo = new JButton("Nuevo");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
        
        btnNuevo.addActionListener(e -> mostrarDialogoCliente(null));
        btnEditar.addActionListener(e -> editarClienteSeleccionado());
        btnEliminar.addActionListener(e -> eliminarClienteSeleccionado());
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelBotones.add(btnNuevo, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.0;
        panelBotones.add(btnEditar, gbc);
        
        gbc.gridx = 2;
        panelBotones.add(btnEliminar, gbc);
        
        // Agregar componentes al panel principal
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(panelBusqueda, gbc);
        
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPanel.add(panelTabla, gbc);
        
        gbc.gridy = 2;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(panelBotones, gbc);
        
        // Agregar el panel de contenido al panel principal con borde
        add(contentPanel, BorderLayout.CENTER);
        
        // Configurar el filtro de búsqueda
        configurarFiltroBusqueda();
        
        // Cargar los datos iniciales
        cargarClientes();
    }
    
    /**
     * Configura el filtro de búsqueda para la tabla de clientes.
     */
    private void configurarFiltroBusqueda() {
        // Filtrar por texto de búsqueda
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            @SuppressWarnings("unused")
            public void insertUpdate(DocumentEvent e) {
                filtrarTabla();
            }
            
            @Override
            @SuppressWarnings("unused")
            public void removeUpdate(DocumentEvent e) {
                filtrarTabla();
            }
            
            @Override
            @SuppressWarnings("unused")
            public void changedUpdate(DocumentEvent e) {
                filtrarTabla();
            }
        });
        
        // Filtrar por tipo de cliente
        cmbTipoFiltro.addActionListener(e -> filtrarTabla());
    }
    
    /**
     * Filtra la tabla de clientes según los criterios de búsqueda.
     */
    private void filtrarTabla() {
        try {
            String texto = txtBuscar.getText().toLowerCase();
            TipoCliente tipoFiltro = (TipoCliente) cmbTipoFiltro.getSelectedItem();
            
            RowFilter<TableModel, Integer> rf = new RowFilter<TableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                    // Filtrar por texto de búsqueda
                    boolean coincideTexto = false;
                    for (int i = 0; i < entry.getValueCount(); i++) {
                        if (entry.getStringValue(i).toLowerCase().contains(texto)) {
                            coincideTexto = true;
                            break;
                        }
                    }
                    
                    // Filtrar por tipo de cliente
                    boolean coincideTipo = true;
                    if (tipoFiltro != null) {
                        TipoCliente tipoCliente = (TipoCliente) entry.getValue(2);
                        coincideTipo = tipoFiltro.equals(tipoCliente);
                    }
                    
                    return coincideTexto && coincideTipo;
                }
            };
            
            sorter.setRowFilter(rf);
        } catch (Exception e) {
            mostrarError("Error al filtrar la tabla: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Carga los clientes desde el servicio y los muestra en la tabla.
     */
    private void cargarClientes() {
        try {
            // Limpiar la tabla
            modeloTabla.setRowCount(0);
            
            // Obtener todos los clientes del servicio
            List<Cliente> clientes = clienteService.listarTodos();
            
            // Llenar la tabla con los clientes
            for (Cliente cliente : clientes) {
                modeloTabla.addRow(new Object[]{
                    cliente.getCedula(),
                    cliente.getNombre(),
                    cliente.getTipo(),
                    cliente.getContacto() != null ? cliente.getContacto() : "N/A",
                    cliente.isCiudadanoOro() ? "Sí" : "No",
                    cliente.getPuntos()
                });
            }
            
            // Ordenar por cédula por defecto
            if (tablaClientes.getRowSorter() != null && tablaClientes.getRowSorter().getSortKeys() != null && tablaClientes.getRowSorter().getSortKeys().isEmpty()) {
                tablaClientes.getRowSorter().toggleSortOrder(0);
            }
            
        } catch (Exception e) {
            mostrarError("Error al cargar los clientes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Muestra el diálogo para agregar o editar un cliente.
     * 
     * @param cliente Cliente a editar, o null para crear uno nuevo
     */
    private void mostrarDialogoCliente(Cliente cliente) {
        JDialogCliente dialog = new JDialogCliente(SwingUtilities.getWindowAncestor(this), cliente);
        dialog.setVisible(true);
        
        if (dialog.isGuardado()) {
            cargarClientes();
        }
    }

    /**
     * Maneja los errores de persistencia mostrando un mensaje al usuario.
     * 
     * @param e Excepción de persistencia
     */
    private void manejarErrorPersistencia(Exception e) {
        String mensaje = "Error al guardar el cliente: ";
        
        if (e.getCause() != null && e.getCause().getMessage().toLowerCase().contains("unique")) {
            mensaje += "Ya existe un cliente con esta cédula.";
        } else {
            mensaje += e.getMessage();
        }
        
        JOptionPane.showMessageDialog(this, 
            mensaje, 
            "Error de persistencia", 
            JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Muestra un mensaje de error en un diálogo.
     * 
     * @param mensaje Mensaje de error a mostrar
     */
    protected void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Obtiene el cliente seleccionado en la tabla.
     * 
     * @return El cliente seleccionado, o null si no hay selección
     */
    private Cliente obtenerClienteSeleccionado() {
        int filaSeleccionada = tablaClientes.getSelectedRow();
        if (filaSeleccionada == -1) {
            return null;
        }
        
        // Obtener la fila real en el modelo, considerando el ordenamiento
        int filaModelo = tablaClientes.convertRowIndexToModel(filaSeleccionada);
        long cedula = (long) modeloTabla.getValueAt(filaModelo, 0);
        
        try {
            return clienteService.buscarPorCedula(cedula).orElse(null);
        } catch (Exception e) {
            mostrarError("Error al obtener el cliente: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Edita el cliente seleccionado en la tabla.
     */
    private void editarClienteSeleccionado() {
        Cliente cliente = obtenerClienteSeleccionado();
        if (cliente == null) {
            mostrarError("Por favor, seleccione un cliente para editar.");
            return;
        }
        
        mostrarDialogoCliente(cliente);
    }
    
    /**
     * Elimina el cliente seleccionado de la tabla.
     */
    private void eliminarClienteSeleccionado() {
        Cliente cliente = obtenerClienteSeleccionado();
        if (cliente == null) {
            mostrarError("Por favor, seleccione un cliente para eliminar.");
            return;
        }
        
        // Verificar si el cliente tiene facturas asociadas
        if (!app.getFacturaService().buscarPorCliente(cliente.getCedula()).isEmpty()) {
            mostrarError("No se puede eliminar el cliente porque tiene facturas asociadas.");
            return;
        }
        
        // Confirmar eliminación
        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro de que desea eliminar al cliente " + cliente.getNombre() + "?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                boolean eliminado = app.getClienteService().eliminarCliente(cliente.getCedula());
                if (eliminado) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Cliente eliminado correctamente",
                        "Eliminación exitosa",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    cargarClientes();
                } else {
                    mostrarError("No se pudo eliminar el cliente. Por favor, intente nuevamente.");
                }
            } catch (Exception e) {
                manejarErrorPersistencia(e);
            }
        }
    }
    
    /**
     * Diálogo para agregar o editar un cliente.
     */
    private class JDialogCliente extends JDialog {
        private final Cliente cliente;
        private boolean guardado = false;
        
        // Componentes de la interfaz
        private JTextField txtCedula;
        private JTextField txtNombre;
        private JComboBox<TipoCliente> cmbTipoCliente;
        private JTextField txtContacto;
        private JCheckBox chkCiudadanoOro;
        private JSpinner spnPuntos;
        
        /**
         * Crea un nuevo diálogo para agregar o editar un cliente.
         * 
         * @param parent Ventana padre
         * @param cliente Cliente a editar, o null para crear uno nuevo
         */
        public JDialogCliente(Window parent, Cliente cliente) {
            super(parent, cliente == null ? "Nuevo Cliente" : "Editar Cliente", Dialog.ModalityType.APPLICATION_MODAL);
            this.cliente = cliente;
            initComponents();
            cargarDatosCliente();
            pack();
            setLocationRelativeTo(parent);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }
        
        /**
         * Inicializa los componentes del diálogo.
         */
        private void initComponents() {
            setLayout(new BorderLayout(10, 10));
            
            // Panel principal con márgenes
            JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
            panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            // Panel de campos del formulario
            JPanel panelCampos = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            // Campo Cédula
            gbc.gridx = 0;
            gbc.gridy = 0;
            panelCampos.add(new JLabel("Cédula:"), gbc);
            
            txtCedula = new JTextField(20);
            txtCedula.setEditable(cliente == null); // Solo editable para nuevo cliente
            gbc.gridx = 1;
            panelCampos.add(txtCedula, gbc);
            
            // Campo Nombre
            gbc.gridx = 0;
            gbc.gridy++;
            panelCampos.add(new JLabel("Nombre:"), gbc);
            
            txtNombre = new JTextField(20);
            gbc.gridx = 1;
            panelCampos.add(txtNombre, gbc);
            
            // Campo Tipo de Cliente
            gbc.gridx = 0;
            gbc.gridy++;
            panelCampos.add(new JLabel("Tipo:"), gbc);
            
            cmbTipoCliente = new JComboBox<>(TipoCliente.values());
            cmbTipoCliente.addActionListener(e -> actualizarCamposSegunTipo());
            gbc.gridx = 1;
            panelCampos.add(cmbTipoCliente, gbc);
            
            // Panel para campos específicos del tipo de cliente
            JPanel panelTipoCliente = new JPanel(new GridBagLayout());
            panelTipoCliente.setBorder(BorderFactory.createTitledBorder("Detalles del Cliente"));
            GridBagConstraints gbcTipo = new GridBagConstraints();
            gbcTipo.insets = new Insets(5, 5, 5, 5);
            gbcTipo.anchor = GridBagConstraints.WEST;
            gbcTipo.fill = GridBagConstraints.HORIZONTAL;
            
            // Campos para clientes ocasionales
            chkCiudadanoOro = new JCheckBox("Ciudadano Oro");
            chkCiudadanoOro.addActionListener(e -> actualizarCamposPuntos());
            gbcTipo.gridx = 0;
            gbcTipo.gridy = 0;
            gbcTipo.gridwidth = 2;
            panelTipoCliente.add(chkCiudadanoOro, gbcTipo);
            
            gbcTipo.gridx = 0;
            gbcTipo.gridy++;
            gbcTipo.gridwidth = 1;
            panelTipoCliente.add(new JLabel("Puntos:"), gbcTipo);
            
            spnPuntos = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
            spnPuntos.setEnabled(false);
            gbcTipo.gridx = 1;
            panelTipoCliente.add(spnPuntos, gbcTipo);
            
            // Campo para clientes corporativos
            gbcTipo.gridx = 0;
            gbcTipo.gridy++;
            gbcTipo.gridwidth = 2;
            panelTipoCliente.add(new JLabel("Contacto:"), gbcTipo);
            
            txtContacto = new JTextField(20);
            gbcTipo.gridy++;
            panelTipoCliente.add(txtContacto, gbcTipo);
            
            // Agregar paneles al diálogo
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 2;
            panelCampos.add(panelTipoCliente, gbc);
            
            // Botones
            JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnGuardar = new JButton("Guardar");
            JButton btnCancelar = new JButton("Cancelar");
            
            btnGuardar.addActionListener(e -> guardarCliente());
            btnCancelar.addActionListener(e -> dispose());
            
            // Configurar tecla Enter para guardar
            getRootPane().setDefaultButton(btnGuardar);
            
            panelBotones.add(btnGuardar);
            panelBotones.add(btnCancelar);
            
            // Agregar todo al panel principal
            panelPrincipal.add(panelCampos, BorderLayout.CENTER);
            panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
            
            add(panelPrincipal, BorderLayout.CENTER);
        }
        
        /**
         * Actualiza los campos según el tipo de cliente seleccionado.
         */
        private void actualizarCamposSegunTipo() {
            TipoCliente tipo = (TipoCliente) cmbTipoCliente.getSelectedItem();
            boolean esOcasional = tipo == TipoCliente.OCASIONALES;
            
            chkCiudadanoOro.setEnabled(esOcasional);
            spnPuntos.setEnabled(esOcasional && chkCiudadanoOro.isSelected());
            
            // Mostrar u ocultar campos según el tipo de cliente
            txtContacto.setEnabled(!esOcasional);
            
            // Si es corporativo, asegurarse que no sea ciudadano de oro
            if (!esOcasional) {
                chkCiudadanoOro.setSelected(false);
            }
        }
        
        /**
         * Actualiza el estado del campo de puntos según la selección de ciudadano de oro.
         */
        private void actualizarCamposPuntos() {
            boolean esCiudadanoOro = chkCiudadanoOro.isSelected();
            spnPuntos.setEnabled(esCiudadanoOro);
            if (!esCiudadanoOro) {
                spnPuntos.setValue(0);
            }
        }
        
        /**
         * Carga los datos del cliente en el formulario.
         */
        private void cargarDatosCliente() {
            if (cliente != null) {
                txtCedula.setText(String.valueOf(cliente.getCedula()));
                txtNombre.setText(cliente.getNombre());
                cmbTipoCliente.setSelectedItem(cliente.getTipo());
                
                if (cliente.getTipo() == TipoCliente.CORPORATIVOS) {
                    txtContacto.setText(cliente.getContacto());
                } else {
                    chkCiudadanoOro.setSelected(cliente.isCiudadanoOro());
                    spnPuntos.setValue(cliente.getPuntos());
                }
            } else {
                // Valores por defecto para nuevo cliente
                cmbTipoCliente.setSelectedItem(TipoCliente.OCASIONALES);
                chkCiudadanoOro.setSelected(false);
                spnPuntos.setValue(0);
            }
            
            // Actualizar estado inicial de los campos
            actualizarCamposSegunTipo();
        }
        
        /**
         * Guarda los datos del cliente.
         */
        private void guardarCliente() {
            try {
                if (validarDatos()) {
                    // Crear o actualizar el cliente
                    Cliente clienteGuardar;
                    long cedula = Long.parseLong(txtCedula.getText().trim());
                    String nombre = txtNombre.getText().trim();
                    TipoCliente tipo = (TipoCliente) cmbTipoCliente.getSelectedItem();
                    String contacto = tipo == TipoCliente.CORPORATIVOS ? txtContacto.getText().trim() : "";
                    
                    if (this.cliente == null) {
                        // Nuevo cliente
                        clienteGuardar = new Cliente(cedula, nombre, tipo, contacto);
                        
                        // Configurar ciudadano de oro si aplica
                        if (tipo == TipoCliente.OCASIONALES) {
                            boolean esCiudadanoOro = chkCiudadanoOro.isSelected();
                            clienteGuardar.setCiudadanoOro(esCiudadanoOro);
                            
                            if (esCiudadanoOro) {
                                int puntos = (Integer) spnPuntos.getValue();
                                if (puntos > 0) {
                                    clienteGuardar.agregarPuntos(puntos);
                                }
                            }
                        }
                        
                        // Registrar el nuevo cliente
                        app.getClienteService().registrarCliente(clienteGuardar);
                    } else {
                        // Actualizar cliente existente
                        clienteGuardar = this.cliente;
                        clienteGuardar.setNombre(nombre);
                        clienteGuardar.setTipo(tipo);
                        
                        if (tipo == TipoCliente.CORPORATIVOS) {
                            clienteGuardar.setContacto(contacto);
                        } else {
                            boolean esCiudadanoOro = chkCiudadanoOro.isSelected();
                            clienteGuardar.setCiudadanoOro(esCiudadanoOro);
                            
                            if (esCiudadanoOro) {
                                int nuevosPuntos = (Integer) spnPuntos.getValue();
                                int puntosActuales = clienteGuardar.getPuntos();
                                
                                if (nuevosPuntos > puntosActuales) {
                                    int puntosAAgregar = nuevosPuntos - puntosActuales;
                                    clienteGuardar.agregarPuntos(puntosAAgregar);
                                }
                            } else {
                                // Si ya no es ciudadano de oro, se mantienen los puntos pero no se pueden modificar
                                // Se podría considerar si se desea resetear los puntos
                            }
                        }
                        
                        // Actualizar el cliente
                        app.getClienteService().actualizarCliente(clienteGuardar);
                    }
                    
                    guardado = true;
                    dispose();
                }
            } catch (NumberFormatException e) {
                mostrarError("La cédula debe ser un número válido.");
            } catch (IllegalArgumentException | IllegalStateException e) {
                mostrarError(e.getMessage());
            } catch (Exception e) {
                manejarErrorPersistencia(e);
            }
        }
        
        /**
         * Valida los datos del formulario.
         * 
         * @return true si los datos son válidos, false en caso contrario
         */
        private boolean validarDatos() {
            // Validar cédula
            String cedulaStr = txtCedula.getText().trim();
            if (cedulaStr.isEmpty()) {
                mostrarError("La cédula es obligatoria.");
                txtCedula.requestFocus();
                return false;
            }
            
            try {
                long cedula = Long.parseLong(cedulaStr);
                if (cedula <= 0) {
                    mostrarError("La cédula debe ser un número positivo.");
                    txtCedula.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                mostrarError("La cédula debe contener solo números.");
                txtCedula.requestFocus();
                return false;
            }
            
            // Validar nombre
            String nombre = txtNombre.getText().trim();
            if (nombre.isEmpty()) {
                mostrarError("El nombre es obligatorio.");
                txtNombre.requestFocus();
                return false;
            }
            
            // Validar que el nombre solo contenga letras y espacios
            if (!nombre.matches("^[\\p{L} .'-]+$")) {
                mostrarError("El nombre solo puede contener letras y espacios.");
                txtNombre.requestFocus();
                return false;
            }
            
            // Validar contacto si es cliente corporativo
            TipoCliente tipo = (TipoCliente) cmbTipoCliente.getSelectedItem();
            if (tipo == TipoCliente.CORPORATIVOS) {
                String contacto = txtContacto.getText().trim();
                if (contacto.isEmpty()) {
                    mostrarError("El contacto es obligatorio para clientes corporativos.");
                    txtContacto.requestFocus();
                    return false;
                }
            }
            
            // Validar puntos si es ciudadano de oro
            if (tipo == TipoCliente.OCASIONALES && chkCiudadanoOro.isSelected()) {
                try {
                    int puntos = (Integer) spnPuntos.getValue();
                    if (puntos < 0) {
                        mostrarError("Los puntos no pueden ser negativos.");
                        spnPuntos.requestFocus();
                        return false;
                    }
                } catch (Exception e) {
                    mostrarError("El valor de puntos no es válido.");
                    spnPuntos.requestFocus();
                    return false;
                }
            }
            
            return true;
        }
        
        /**
         * Muestra un mensaje de error en un diálogo.
         * 
         * @param mensaje Mensaje de error a mostrar
         */
        private void mostrarError(String mensaje) {
            JOptionPane.showMessageDialog(this, 
                mensaje, 
                "Error de validación", 
                JOptionPane.ERROR_MESSAGE);
        }
        
        /**
         * Maneja los errores de persistencia.
         * 
         * @param e Excepción ocurrida
         */
        private void manejarErrorPersistencia(Exception e) {
            String mensaje = "Error al guardar el cliente: " + e.getMessage();
            if (e.getCause() != null) {
                mensaje += "\nCausa: " + e.getCause().getMessage();
            }
            mostrarError(mensaje);
        }
        
        /**
         * Indica si se guardaron los cambios en el cliente.
         * 
         * @return true si se guardaron los cambios, false en caso contrario
         */
        public boolean isGuardado() {
            return guardado;
        }
    }
}
