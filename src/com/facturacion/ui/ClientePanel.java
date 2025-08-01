package com.facturacion.ui;

import com.facturacion.App;
import com.facturacion.model.Cliente;
import com.facturacion.model.TipoCliente;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
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
    private JComboBox<String> cmbTipoCliente;
    
    // Filtro para la tabla
    private TableRowSorter<TableModel> sorter;
    
    /**
     * Crea una nueva instancia del panel de clientes.
     * 
     * @param app Referencia a la aplicación principal
     */
    public ClientePanel(App app) {
        super(app);
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
        panelBusqueda.setBorder(javax.swing.BorderFactory.createTitledBorder("Búsqueda"));
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panelBusqueda.add(new JLabel("Buscar:"), gbc);
        
        txtBuscar = new JTextField(20);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panelBusqueda.add(txtBuscar, gbc);
        
        cmbTipoCliente = new JComboBox<>(new String[]{"Todos", "Ocasionales", "Corporativos"});
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        panelBusqueda.add(cmbTipoCliente, gbc);
        
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
            public void insertUpdate(DocumentEvent e) {
                filtrarTabla();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrarTabla();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrarTabla();
            }
        });
        
        // Filtrar por tipo de cliente
        cmbTipoCliente.addActionListener(e -> filtrarTabla());
    }
    
    /**
     * Aplica los filtros de búsqueda a la tabla.
     */
    private void filtrarTabla() {
        String texto = txtBuscar.getText().toLowerCase();
        String tipoSeleccionado = (String) cmbTipoCliente.getSelectedItem();
        
        sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                // Filtrar por texto de búsqueda (cédula, nombre, contacto)
                String cedula = entry.getStringValue(0).toLowerCase();
                String nombre = entry.getStringValue(1).toLowerCase();
                String tipo = entry.getStringValue(2);
                String contacto = entry.getStringValue(3).toLowerCase();
                
                boolean coincideTexto = cedula.contains(texto) || 
                                      nombre.contains(texto) || 
                                      contacto.contains(texto);
                
                // Filtrar por tipo de cliente
                boolean coincideTipo = tipoSeleccionado.equals("Todos") ||
                    (tipoSeleccionado.equals("Ocasionales") && tipo.equals("Ocasional")) ||
                    (tipoSeleccionado.equals("Corporativos") && tipo.equals("Corporativo"));
                
                return coincideTexto && coincideTipo;
            }
        });
    }
    
    /**
     * Carga los clientes desde el servicio y los muestra en la tabla.
     */
    private void cargarClientes() {
        // Limpiar la tabla
        modeloTabla.setRowCount(0);
        
        try {
            // Obtener todos los clientes
            List<Cliente> clientes = app.getClienteService().listarTodos();
            
            // Agregar cada cliente a la tabla
            for (Cliente cliente : clientes) {
                modeloTabla.addRow(new Object[]{
                    String.format("%d-%s", 
                        (int)(cliente.getCedula() / 1000), 
                        String.format("%04d", cliente.getCedula() % 10000)),
                    cliente.getNombre(),
                    cliente.getTipo().getDescripcion(),
                    cliente.getContacto() != null ? cliente.getContacto() : "N/A",
                    cliente.isCiudadanoOro(),
                    cliente.getPuntos()
                });
            }
            
            // Ordenar por cédula por defecto
            tablaClientes.getRowSorter().toggleSortOrder(0);
            
        } catch (Exception e) {
            mostrarError("Error al cargar los clientes: " + e.getMessage());
        }
    }
    
    /**
     * Muestra el diálogo para agregar o editar un cliente.
     * 
     * @param cliente Cliente a editar, o null para crear uno nuevo
     */
    private void mostrarDialogoCliente(Cliente cliente) {
        // Crear el diálogo
        JDialogCliente dialogo = new JDialogCliente((JDialog)getTopLevelAncestor(), this, cliente);
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
        
        // Si se guardaron los cambios, actualizar la tabla
        if (dialogo.isGuardado()) {
            cargarClientes();
        }
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
        
        // Obtener la cédula del cliente seleccionado (eliminar guiones y espacios)
        String cedulaStr = (String) modeloTabla.getValueAt(
            tablaClientes.convertRowIndexToModel(filaSeleccionada), 0);
        long cedula = Long.parseLong(cedulaStr.replaceAll("[^0-9]", ""));
        
        // Buscar el cliente en el servicio
        return app.getClienteService().buscarPorCedula(cedula).orElse(null);
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
        
        // Confirmar la eliminación
        boolean confirmar = confirmar(
            "¿Está seguro que desea eliminar al cliente " + cliente.getNombre() + "?");
        
        if (confirmar) {
            try {
                // Eliminar el cliente
                boolean eliminado = app.getClienteService().eliminarCliente(cliente.getCedula());
                
                if (eliminado) {
                    mostrarInformacion("Cliente eliminado correctamente.");
                    cargarClientes();
                } else {
                    mostrarError("No se pudo eliminar el cliente.");
                }
            } catch (Exception e) {
                mostrarError("Error al eliminar el cliente: " + e.getMessage());
            }
        }
    }
    
    /**
     * Clase interna para el diálogo de edición de clientes.
     */
    private class JDialogCliente extends JDialog {
        private static final long serialVersionUID = 1L;
        
        // Componentes del formulario
        private JTextField txtCedula;
        private JTextField txtNombre;
        private JComboBox<TipoCliente> cmbTipoCliente;
        private JTextField txtContacto;
        private JPanel panelContacto;
        private JCheckBox chkCiudadanoOro;
        private JSpinner spnPuntos;
        private JButton btnGuardar;
        private JButton btnCancelar;
        private boolean guardado = false;
        
        private final ClientePanel panelPadre;
        private final Cliente cliente;
        
        /**
         * Crea un nuevo diálogo para agregar o editar un cliente.
         * 
         * @param panelPadre Panel padre que crea el diálogo
         * @param cliente Cliente a editar, o null para crear uno nuevo
         */
        public JDialogCliente(JDialog parent, ClientePanel panelPadre, Cliente cliente) {
            super(parent, cliente == null ? "Nuevo Cliente" : "Editar Cliente", true);
            this.panelPadre = panelPadre;
            this.cliente = cliente;
            
            initComponents();
            pack();
            setLocationRelativeTo(panelPadre);
        }
        
        /**
         * Inicializa los componentes del diálogo.
         */
        private void initComponents() {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            
            // Cédula
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(new JLabel("Cédula:"), gbc);
            
            txtCedula = new JTextField(15);
            txtCedula.setEditable(cliente == null); // Solo editable para nuevos clientes
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(txtCedula, gbc);
            
            // Nombre
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 1;
            add(new JLabel("Nombre:"), gbc);
            
            txtNombre = new JTextField(30);
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(txtNombre, gbc);
            
            // Tipo de cliente
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 1;
            add(new JLabel("Tipo:"), gbc);
            
            cmbTipoCliente = new JComboBox<>(TipoCliente.values());
            cmbTipoCliente.addActionListener(e -> actualizarCamposTipo());
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(cmbTipoCliente, gbc);
            
            // Panel de contacto (inicialmente oculto)
            panelContacto = new JPanel(new GridBagLayout());
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 3;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(panelContacto, gbc);
            
            // Ciudadano Oro
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 1;
            add(new JLabel("Ciudadano Oro:"), gbc);
            
            chkCiudadanoOro = new JCheckBox();
            chkCiudadanoOro.addActionListener(e -> actualizarCamposPuntos());
            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.WEST;
            add(chkCiudadanoOro, gbc);
            
            // Puntos (inicialmente deshabilitado)
            gbc.gridx = 2;
            gbc.anchor = GridBagConstraints.EAST;
            add(new JLabel("Puntos:"), gbc);
            
            spnPuntos = new JSpinner(new javax.swing.SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
            spnPuntos.setEnabled(false);
            gbc.gridx = 3;
            gbc.anchor = GridBagConstraints.WEST;
            add(spnPuntos, gbc);
            
            // Panel de botones
            JPanel panelBotones = new JPanel();
            btnGuardar = new JButton("Guardar");
            btnCancelar = new JButton("Cancelar");
            
            btnGuardar.addActionListener(e -> guardarCliente());
            btnCancelar.addActionListener(e -> dispose());
            
            panelBotones.add(btnGuardar);
            panelBotones.add(btnCancelar);
            
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 4;
    
    /**
     * Inicializa los componentes del diálogo.
     */
    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Cédula
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Cédula:"), gbc);
        
        txtCedula = new JTextField(15);
        txtCedula.setEditable(cliente == null); // Solo editable para nuevos clientes
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(txtCedula, gbc);
        
        // Nombre
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        add(new JLabel("Nombre:"), gbc);
        
        txtNombre = new JTextField(30);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(txtNombre, gbc);
        
        // Tipo de cliente
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        add(new JLabel("Tipo:"), gbc);
        
        cmbTipoCliente = new JComboBox<>(TipoCliente.values());
        cmbTipoCliente.addActionListener(e -> actualizarCamposTipo());
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(cmbTipoCliente, gbc);
        
        // Panel de contacto (inicialmente oculto)
        panelContacto = new JPanel(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(panelContacto, gbc);
        
        // Ciudadano Oro
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        add(new JLabel("Ciudadano Oro:"), gbc);
        
        chkCiudadanoOro = new JCheckBox();
        chkCiudadanoOro.addActionListener(e -> actualizarCamposPuntos());
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(chkCiudadanoOro, gbc);
        
        // Puntos (inicialmente deshabilitado)
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Puntos:"), gbc);
        
        spnPuntos = new JSpinner(new javax.swing.SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        spnPuntos.setEnabled(false);
        gbc.gridx = 3;
        gbc.anchor = GridBagConstraints.WEST;
        add(spnPuntos, gbc);
        
        // Panel de botones
        JPanel panelBotones = new JPanel();
        btnGuardar = new JButton("Guardar");
        btnCancelar = new JButton("Cancelar");
        
        btnGuardar.addActionListener(e -> guardarCliente());
        btnCancelar.addActionListener(e -> dispose());
        
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        add(panelBotones, gbc);
        
        // Cargar los datos del cliente si se está editando
        if (cliente != null) {
            cargarDatosCliente();
        } else {
            // Nuevo cliente - valores por defecto
            cmbTipoCliente.setSelectedItem(TipoCliente.OCASIONALES);
            actualizarCamposTipo();
            actualizarCamposPuntos();
        }
        
        // Configurar el comportamiento al cerrar
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                dispose();
            }
        });
        
        // Ajustar el tamaño del diálogo
        pack();
        setResizable(false);
        setLocationRelativeTo(panelPadre);
    }
    
    /**
     * Carga los datos del cliente en el formulario.
     */
    private void cargarDatosCliente() {
        txtCedula.setText(String.format("%d-%s", 
            (int)(cliente.getCedula() / 1000), 
            String.format("%04d", cliente.getCedula() % 10000)));
        
        txtNombre.setText(cliente.getNombre());
        cmbTipoCliente.setSelectedItem(cliente.getTipo());
        
        // Actualizar los campos según el tipo de cliente
        actualizarCamposTipo();
        
        // Verificar si el cliente es ciudadano de oro y actualizar los campos
        chkCiudadanoOro.setSelected(cliente.isCiudadanoOro());
        spnPuntos.setValue(cliente.getPuntos());
        
        actualizarCamposPuntos();
    }
    
    /**
     * Actualiza los campos del formulario según el tipo de cliente seleccionado.
     */
    private void actualizarCamposTipo() {
        TipoCliente tipo = (TipoCliente) cmbTipoCliente.getSelectedItem();
        
        // Limpiar el panel de contacto
        panelContacto.removeAll();
        
        if (tipo == TipoCliente.CORPORATIVOS) {
            // Mostrar campo de contacto para clientes corporativos
            panelContacto.setVisible(true);
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 0, 5);
            gbc.anchor = GridBagConstraints.WEST;
            
            gbc.gridx = 0;
            gbc.gridy = 0;
            panelContacto.add(new JLabel("Persona de contacto:"), gbc);
            
            txtContacto = new JTextField(25);
            gbc.gridx = 1;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panelContacto.add(txtContacto, gbc);
        } else {
            // Ocultar el panel de contacto para clientes ocasionales
            panelContacto.setVisible(false);
        }
        
        // Revalidar y redibujar el diálogo
        revalidate();
        pack();
    }
    
    /**
     * Actualiza el estado del campo de puntos según la selección de Ciudadano Oro.
     */
    private void actualizarCamposPuntos() {
        boolean esCiudadanoOro = chkCiudadanoOro.isSelected();
        spnPuntos.setEnabled(esCiudadanoOro);
        
        if (!esCiudadanoOro) {
            spnPuntos.setValue(0);
        }
    }
    
    /**
     * Valida los datos del formulario.
     * 
     * @return true si los datos son válidos, false en caso contrario
     */
    private boolean validarDatos() {
        // Validar cédula
        try {
            String cedulaStr = txtCedula.getText().trim().replaceAll("[^0-9]", "");
            if (cedulaStr.length() < 9 || cedulaStr.length() > 10) {
                mostrarError("La cédula debe tener entre 9 y 10 dígitos.");
                txtCedula.requestFocus();
                return false;
            }
            
            long cedula = Long.parseLong(cedulaStr);
            
            // Si es un nuevo cliente o la cédula ha cambiado, verificar que no exista
            if (cliente == null || cliente.getCedula() != cedula) {
                if (app.getClienteService().buscarPorCedula(cedula).isPresent()) {
                    mostrarError("Ya existe un cliente con esta cédula.");
                    txtCedula.requestFocus();
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            mostrarError("La cédula debe ser un número válido.");
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
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+")) {
            mostrarError("El nombre solo puede contener letras y espacios.");
            txtNombre.requestFocus();
            return false;
        }
        
        // Validar contacto para clientes corporativos
        TipoCliente tipo = (TipoCliente) cmbTipoCliente.getSelectedItem();
        if (tipo == TipoCliente.CORPORATIVOS) {
            String contacto = txtContacto.getText().trim();
            if (contacto.isEmpty()) {
                mostrarError("El contacto es obligatorio para clientes corporativos.");
                txtContacto.requestFocus();
                return false;
            }
            
            // Validar formato del contacto (nombre apellido)
            if (!contacto.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+\\s+[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*")) {
                mostrarError("El contacto debe incluir al menos un nombre y un apellido.");
                txtContacto.requestFocus();
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Guarda el cliente en la base de datos.
     */
     * Maneja la lógica de creación o actualización de clientes.
     */
    private void guardarCliente() {
        if (!validarDatos()) {
            return;
        }
        
        try {
            // Obtener los datos del formulario
            long cedula = Long.parseLong(txtCedula.getText().trim().replaceAll("[^0-9]", ""));
            String nombre = txtNombre.getText().trim();
            TipoCliente tipo = (TipoCliente) cmbTipoCliente.getSelectedItem();
            boolean esCiudadanoOro = chkCiudadanoOro.isSelected();
            int puntos = esCiudadanoOro ? (int) spnPuntos.getValue() : 0;
            
            // Obtener el contacto si es un cliente corporativo
            String contactoCliente = null;
            if (tipo == TipoCliente.CORPORATIVOS) {
                contactoCliente = txtContacto != null ? txtContacto.getText().trim() : "";
            }
            
            Cliente clienteGuardar = prepararClienteParaGuardar(cedula, nombre, tipo, contactoCliente, esCiudadanoOro, puntos);
            
            // Determinar si es una operación de creación o actualización
            boolean esNuevo = (cliente == null);
            
            // Guardar el cliente
            if (esNuevo) {
                ClientePanel.this.app.getClienteService().registrarCliente(clienteGuardar);
                JOptionPane.showMessageDialog(this, 
                    "Cliente registrado exitosamente.", 
                    "Éxito", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                ClientePanel.this.app.getClienteService().actualizarCliente(clienteGuardar);
                JOptionPane.showMessageDialog(this, 
                    "Cliente actualizado exitosamente.", 
                    "Éxito", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
            guardado = true;
            dispose();
            
        } catch (javax.persistence.PersistenceException pe) {
            manejarErrorPersistencia(pe);
        } catch (IllegalArgumentException iae) {
            JOptionPane.showMessageDialog(this, 
                "Error de validación: " + iae.getMessage(), 
                "Error de validación", 
                JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error inesperado al guardar el cliente: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Prepara el objeto Cliente con los datos del formulario.
     * 
     * @param cedula Número de cédula del cliente
     * @param nombre Nombre del cliente
     * @param tipo Tipo de cliente (OCASIONALES o CORPORATIVOS)
     * @param contactoCliente Contacto del cliente (solo para corporativos)
     * @param esCiudadanoOro Indica si es cliente ciudadano de oro
     * @param puntos Puntos del cliente (solo para ciudadanos de oro)
     * @return Objeto Cliente listo para ser guardado
     */
    private Cliente prepararClienteParaGuardar(long cedula, String nombre, TipoCliente tipo, 
                                             String contactoCliente, boolean esCiudadanoOro, int puntos) {
        if (cliente == null) {
            // Crear nuevo cliente
            Cliente nuevoCliente = new Cliente(cedula, nombre, tipo, contactoCliente);
            
            // Configurar ciudadano de oro si aplica
            if (tipo == TipoCliente.OCASIONALES) {
                nuevoCliente.setCiudadanoOro(esCiudadanoOro);
                if (esCiudadanoOro) {
                    nuevoCliente.agregarPuntos(puntos);
                }
            }
            
            return nuevoCliente;
        } else {
            // Actualizar cliente existente
            cliente.setNombre(nombre);
            cliente.setTipo(tipo);
            
            // Actualizar el contacto si es un cliente corporativo
            if (tipo == TipoCliente.CORPORATIVOS) {
                cliente.setContacto(contactoCliente);
            }
            
            // Actualizar estado de ciudadano de oro y puntos
            if (tipo == TipoCliente.OCASIONALES) {
                if (esCiudadanoOro) {
                    if (!cliente.isCiudadanoOro()) {
                        cliente.setCiudadanoOro(true);
                    }
                    // Actualizar puntos manteniendo los existentes
                    int puntosActuales = cliente.getPuntos();
                    int diferenciaPuntos = puntos - puntosActuales;
                    if (diferenciaPuntos > 0) {
                        cliente.agregarPuntos(diferenciaPuntos);
                    } else if (diferenciaPuntos < 0) {
                        // No permitir canjear más puntos de los disponibles
                        cliente.canjearPuntos(-diferenciaPuntos);
                    }
                } else {
                    cliente.setCiudadanoOro(false);
                }
            }
            
            return cliente;
        }
    }
    
    /**
     * Maneja los errores de persistencia mostrando mensajes de error apropiados.
     * 
     * @param pe Excepción de persistencia
     */
    private void manejarErrorPersistencia(javax.persistence.PersistenceException pe) {
        String mensaje = "Error al guardar el cliente: ";
        
        // Verificar si es una violación de restricción única (cliente duplicado)
        if (pe.getCause() != null && pe.getCause().getMessage().contains("duplicate key")) {
            mensaje += "Ya existe un cliente con esta cédula.";
        } else {
            mensaje += pe.getMessage();
        }
        
        JOptionPane.showMessageDialog(this, 
            mensaje, 
            "Error de persistencia", 
            JOptionPane.ERROR_MESSAGE);
    }
    }
    
    /**
     * Muestra un mensaje de error en un diálogo.
     * 
     * @param mensaje Mensaje de error a mostrar
     */
    protected void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
