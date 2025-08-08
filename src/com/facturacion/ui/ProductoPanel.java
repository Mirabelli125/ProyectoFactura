package com.facturacion.ui;

import com.facturacion.App;
import com.facturacion.model.Impuesto;
import com.facturacion.model.Producto;
import com.facturacion.model.ProductoNoPerecedero;
import com.facturacion.model.ProductoPerecedero;
import com.facturacion.service.ProductoService;
import com.facturacion.service.impl.ProductoServiceImpl;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Panel para el mantenimiento de productos.
 * Permite listar, agregar, modificar y eliminar productos.
 */
public class ProductoPanel extends BasePanel {
    
    protected final App app;
    private JTable tablaProductos;
    private TableRowSorter<TableModel> sorter;
    private JTextField txtBuscar;
    private JComboBox<String> cmbTipoProducto;
    private JButton btnNuevo;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnRefrescar;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private JButton btnAgregar;
    private JButton btnActualizar;
    private JComboBox<String> cmbTipoBusqueda;
    
    // Servicio
    private final ProductoService productoService;
    
    /**
     * Crea una nueva instancia del panel de productos.
     * 
     * @param app La aplicación principal
     */
    public ProductoPanel(App app) {
        super(app);
        this.app = app;
        this.productoService = new ProductoServiceImpl(null); // Repository will be injected properly later
        initComponents();
        
        // Inicializar los listeners de eventos
        if (btnAgregar != null) {
            btnAgregar.addActionListener(e -> mostrarDialogoNuevoProducto());
        }
        
        if (btnEditar != null) {
            btnEditar.addActionListener(e -> editarProducto());
        }
        
        if (btnEliminar != null) {
            btnEliminar.addActionListener(e -> eliminarProducto());
        }
        
        if (btnActualizar != null) {
            btnActualizar.addActionListener(e -> cargarProductos());
        }
        
        // Configurar el filtro de búsqueda
        if (txtBuscar != null) {
            txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) { filtrarProductos(); }
                @Override
                public void removeUpdate(DocumentEvent e) { filtrarProductos(); }
                @Override
                public void changedUpdate(DocumentEvent e) { filtrarProductos(); }
            });
        }
        
        // Cargar los datos iniciales
        cargarProductos();
    }
    
    /**
     * Inicializa los componentes de la interfaz de usuario.
     */
    private void initComponents() {
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
        
        cmbTipoProducto = new JComboBox<>(new String[]{"Todos", "Perecedero", "No perecedero"});
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        panelBusqueda.add(cmbTipoProducto, gbc);
        
        // Panel de la tabla
        JPanel panelTabla = new JPanel(new GridBagLayout());
        panelTabla.setBorder(javax.swing.BorderFactory.createTitledBorder("Productos"));
        
        // Crear la tabla de productos
        DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"Código", "Nombre", "Precio", "Impuesto", "Cantidad", "Tipo", "Vencimiento"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer que la tabla no sea editable directamente
            }
        };
        
        tablaProductos = new JTable(modeloTabla);
        sorter = new TableRowSorter<>(modeloTabla);
        tablaProductos.setRowSorter(sorter);
        
        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        scrollPane.setPreferredSize(new Dimension(900, 400));
        
        panelTabla.add(scrollPane, gbc);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new GridBagLayout());
        
        // Inicializar botones con nombres consistentes
        btnAgregar = new JButton("Nuevo");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
        
        // Configurar acciones de los botones
        btnAgregar.addActionListener(e -> mostrarDialogoNuevoProducto());
        btnEditar.addActionListener(e -> editarProducto());
        btnEliminar.addActionListener(e -> eliminarProducto());
        
        // Deshabilitar botones de edición y eliminación inicialmente
        btnEditar.setEnabled(false);
        btnEliminar.setEnabled(false);
        
        // Configurar el panel de botones
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelBotones.add(btnAgregar, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        panelBotones.add(btnEditar, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.5;
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
        
        // Configurar la selección de filas en la tabla
        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            boolean filaSeleccionada = tablaProductos.getSelectedRow() != -1;
            btnEditar.setEnabled(filaSeleccionada);
            btnEliminar.setEnabled(filaSeleccionada);
        });
        
        // Configurar el filtro de búsqueda
        configurarFiltroBusqueda();
        
        // Cargar los datos iniciales en un hilo separado para no bloquear la UI
        SwingUtilities.invokeLater(this::cargarProductos);
    }
    
    private void configurarFiltroBusqueda() {
        // Filtrar por texto de búsqueda
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrarProductos();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrarProductos();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrarProductos();
            }
        });
        
        // Filtrar por tipo de producto
        cmbTipoProducto.addActionListener(e -> filtrarProductos());
    }
    
    // Carga los productos desde el servicio y los muestra en la tabla.
    private void cargarProductos() {
        // Limpiar la tabla
        DefaultTableModel modeloTabla = (DefaultTableModel) tablaProductos.getModel();
        modeloTabla.setRowCount(0);
        
        try {
            // Obtener todos los productos
            List<Producto> productos = app.getProductoService().listarTodos();
            
            // Agregar cada producto a la tabla
            for (Producto producto : productos) {
                Object[] fila = new Object[7];
                fila[0] = producto.getCodigo();
                fila[1] = producto.getNombre();
                fila[2] = String.format("₡%,.2f", producto.getPrecio());
                fila[3] = producto.getImpuesto().getDescripcion();
                fila[4] = producto.getCantidadProducto();
                
                if (producto instanceof ProductoPerecedero) {
                    ProductoPerecedero pp = (ProductoPerecedero) producto;
                    fila[5] = "Perecedero";
                    fila[6] = pp.getFechaVencimiento().format(DATE_FORMATTER);
                } else {
                    fila[5] = "No Perecedero";
                    fila[6] = "";
                }
                
                modeloTabla.addRow(fila);
            }
            
            // Ordenar por código de producto por defecto
            if (tablaProductos.getRowSorter() != null) {
                tablaProductos.getRowSorter().toggleSortOrder(0);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar los productos: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filtrarProductos() {
        if (sorter == null || txtBuscar == null || cmbTipoProducto == null) {
            return;
        }
        
        String textoBusqueda = txtBuscar.getText().trim().toLowerCase();
        String tipoSeleccionado = cmbTipoProducto.getSelectedItem().toString().toLowerCase();
        
        RowFilter<TableModel, Object> filtro = new RowFilter<TableModel, Object>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Object> entry) {
                TableModel model = entry.getModel();
                int row = Integer.parseInt(entry.getIdentifier().toString());
                
                // Filtrar por texto de búsqueda (nombre y código)
                String nombre = model.getValueAt(row, 1).toString().toLowerCase();
                String codigo = model.getValueAt(row, 0).toString().toLowerCase();
                boolean coincideTexto = nombre.contains(textoBusqueda) || 
                                      codigo.contains(textoBusqueda);
                
                // Filtrar por tipo de producto
                String tipo = model.getValueAt(row, 5).toString().toLowerCase();
                boolean coincideTipo = tipoSeleccionado.equals("todos") || 
                                     tipo.contains(tipoSeleccionado);
                
                return coincideTexto && coincideTipo;
            }
        };
        
        sorter.setRowFilter(filtro);
    }
    
    /**
     * Muestra el diálogo para agregar un nuevo producto.
     */
    private void mostrarDialogoNuevoProducto() {
        try {
            JDialogProducto dialog = new JDialogProducto(SwingUtilities.getWindowAncestor(this), null);
            dialog.setVisible(true);
            if (dialog.isGuardado()) {
                cargarProductos();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al mostrar el diálogo de producto: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Muestra el diálogo para editar el producto seleccionado.
     */
    private void editarProducto() {
        int filaSeleccionada = tablaProductos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor, seleccione un producto para editar.",
                "Selección requerida",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int idProducto = (int) tablaProductos.getValueAt(filaSeleccionada, 0);
            Producto producto = app.getProductoService().buscarProductoPorId(idProducto);
            
            if (producto == null) {
                JOptionPane.showMessageDialog(this,
                    "No se pudo encontrar el producto seleccionado.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            JDialogProducto dialog = new JDialogProducto(SwingUtilities.getWindowAncestor(this), producto);
            dialog.setVisible(true);
            
            if (dialog.isGuardado()) {
                cargarProductos();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al editar el producto: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Elimina el producto seleccionado de la base de datos.
     */
    private void eliminarProducto() {
        int filaSeleccionada = tablaProductos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Debe seleccionar un producto para eliminar",
                "Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirmar eliminación
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de que desea eliminar el producto seleccionado?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                // Obtener el ID del producto seleccionado
                int idProducto = (int) tablaProductos.getValueAt(filaSeleccionada, 0);

                // Eliminar el producto
                app.getProductoService().eliminarProducto(idProducto);

                // Recargar la lista de productos
                cargarProductos();

                JOptionPane.showMessageDialog(this,
                    "Producto eliminado correctamente",
                    "Eliminación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error al eliminar el producto: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    
    protected void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, 
            mensaje, 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Diálogo para agregar o editar un producto.
     */
    private class JDialogProducto extends JDialog {
        private final Producto producto;
        private boolean guardado = false;
        
        // Componentes de la interfaz
        private final JTextField txtCodigo = new JTextField(15);
        private final JTextField txtNombre = new JTextField(15);
        private final JTextField txtPrecio = new JTextField(15);
        private final JComboBox<Impuesto> cmbImpuesto = new JComboBox<>(Impuesto.values());
        private final JTextField txtCantidad = new JTextField(15);
        private final JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"Perecedero", "No perecedero"});
        private final JTextField txtFechaVencimiento = new JTextField(15);
        private final JButton btnGuardar = new JButton("Guardar");
        private final JButton btnCancelar = new JButton("Cancelar");
        private final JLabel lblFechaVencimiento = new JLabel("Fecha Vencimiento (dd/MM/yyyy):");
        private final JPanel contentPanel = new JPanel(new BorderLayout());
        private final JPanel panelCampos = new JPanel(new GridBagLayout());
        private final JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        public JDialogProducto(Window owner, Producto producto) {
            super(owner, producto == null ? "Nuevo Producto" : "Editar Producto", ModalityType.APPLICATION_MODAL);
            this.producto = producto;
            initComponents();
            pack();
            setLocationRelativeTo(owner);
        }
        
        public boolean isGuardado() {
            return guardado;
        }

        private void initComponents() {
            setLayout(new BorderLayout(10, 10));
            
            // Panel principal
            contentPanel = new JPanel(new BorderLayout(10, 10));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            // Panel de campos
            panelCampos = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(5, 5, 5, 5);
            
            // Código
            gbc.gridx = 0;
            gbc.gridy = 0;
            panelCampos.add(new JLabel("Código:"), gbc);
            
            txtCodigo = new JTextField(15);
            txtCodigo.setEditable(false);
            gbc.gridx = 1;
            panelCampos.add(txtCodigo, gbc);
            
            // Nombre
            gbc.gridx = 0;
            gbc.gridy = 1;
            panelCampos.add(new JLabel("Nombre:"), gbc);
            
            txtNombre = new JTextField(15);
            gbc.gridx = 1;
            panelCampos.add(txtNombre, gbc);
            
            // Precio
            gbc.gridx = 0;
            gbc.gridy = 2;
            panelCampos.add(new JLabel("Precio:"), gbc);
            
            txtPrecio = new JTextField(15);
            gbc.gridx = 1;
            panelCampos.add(txtPrecio, gbc);
            
            // Impuesto
            gbc.gridx = 0;
            gbc.gridy = 3;
            panelCampos.add(new JLabel("Impuesto:"), gbc);
            
            cmbImpuesto = new JComboBox<>(Impuesto.values());
            gbc.gridx = 1;
            panelCampos.add(cmbImpuesto, gbc);
            
            // Cantidad
            gbc.gridx = 0;
            gbc.gridy = 4;
            panelCampos.add(new JLabel("Cantidad:"), gbc);
            
            txtCantidad = new JTextField(15);
            gbc.gridx = 1;
            panelCampos.add(txtCantidad, gbc);
            
            // Tipo de producto
            gbc.gridx = 0;
            gbc.gridy = 5;
            panelCampos.add(new JLabel("Tipo:"), gbc);
            
            cmbTipo = new JComboBox<>(new String[]{"Perecedero", "No perecedero"});
            cmbTipo.addActionListener(e -> actualizarVisibilidadFechaVencimiento());
            gbc.gridx = 1;
            panelCampos.add(cmbTipo, gbc);
            
            // Fecha de vencimiento (inicialmente oculta)
            lblFechaVencimiento = new JLabel("Fecha Vencimiento (dd/MM/yyyy):");
            gbc.gridx = 0;
            gbc.gridy = 6;
            panelCampos.add(lblFechaVencimiento, gbc);
            
            txtFechaVencimiento = new JTextField(15);
            gbc.gridx = 1;
            panelCampos.add(txtFechaVencimiento, gbc);
            
            // Actualizar visibilidad inicial
            actualizarVisibilidadFechaVencimiento();
            
            // Panel de botones
            panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            
            btnGuardar = new JButton("Guardar");
            btnGuardar.addActionListener(e -> guardarProducto());
            panelBotones.add(btnGuardar);
            
            btnCancelar = new JButton("Cancelar");
            btnCancelar.addActionListener(e -> dispose());
            panelBotones.add(btnCancelar);
            
            // Agregar componentes al panel principal
            contentPanel.add(panelCampos, BorderLayout.CENTER);
            contentPanel.add(panelBotones, BorderLayout.SOUTH);
            
            add(contentPanel, BorderLayout.CENTER);
            
            // Cargar datos del producto si existe
            if (producto != null) {
                cargarDatosProducto();
            }
            
            // Configurar el diálogo
            setResizable(false);
            pack();
            setLocationRelativeTo(getParent());
        }

        private void cargarDatosProducto() {
            if (producto == null) return;
            
            txtCodigo.setText(String.valueOf(producto.getCodigo()));
            txtNombre.setText(producto.getNombre());
            txtPrecio.setText(String.valueOf(producto.getPrecio()));
            cmbImpuesto.setSelectedItem(producto.getImpuesto());
            txtCantidad.setText(String.valueOf(producto.getCantidadProducto()));
            
        // Panel de campos
        panelCampos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
            
        // Código
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelCampos.add(new JLabel("Código:"), gbc);
            
        txtCodigo = new JTextField(15);
        txtCodigo.setEditable(false);
        gbc.gridx = 1;
        panelCampos.add(txtCodigo, gbc);
            
        // Nombre
        gbc.gridx = 0;
        gbc.gridy = 1;
        panelCampos.add(new JLabel("Nombre:"), gbc);
            
        txtNombre = new JTextField(15);
        gbc.gridx = 1;
        panelCampos.add(txtNombre, gbc);
            
        // Precio
        gbc.gridx = 0;
        gbc.gridy = 2;
        panelCampos.add(new JLabel("Precio:"), gbc);
            
        txtPrecio = new JTextField(15);
        gbc.gridx = 1;
        panelCampos.add(txtPrecio, gbc);
            
        // Impuesto
        gbc.gridx = 0;
        gbc.gridy = 3;
        panelCampos.add(new JLabel("Impuesto:"), gbc);
            
        cmbImpuesto = new JComboBox<>(Impuesto.values());
        gbc.gridx = 1;
        panelCampos.add(cmbImpuesto, gbc);
            
        // Cantidad
        gbc.gridx = 0;
        gbc.gridy = 4;
        panelCampos.add(new JLabel("Cantidad:"), gbc);
            
        txtCantidad = new JTextField(15);
        gbc.gridx = 1;
        panelCampos.add(txtCantidad, gbc);
            
        // Tipo de producto
        gbc.gridx = 0;
        gbc.gridy = 5;
        panelCampos.add(new JLabel("Tipo:"), gbc);
            
        cmbTipo = new JComboBox<>(new String[]{"Perecedero", "No perecedero"});
        cmbTipo.addActionListener(e -> actualizarVisibilidadFechaVencimiento());
        gbc.gridx = 1;
        panelCampos.add(cmbTipo, gbc);
            
        // Fecha de vencimiento (inicialmente oculta)
        lblFechaVencimiento = new JLabel("Fecha Vencimiento (dd/MM/yyyy):");
        gbc.gridx = 0;
        gbc.gridy = 6;
        panelCampos.add(lblFechaVencimiento, gbc);
            
        txtFechaVencimiento = new JTextField(15);
        gbc.gridx = 1;
        panelCampos.add(txtFechaVencimiento, gbc);
            
        // Actualizar visibilidad inicial
        actualizarVisibilidadFechaVencimiento();
            
        // Panel de botones
        panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            
        btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> guardarProducto());
        panelBotones.add(btnGuardar);
            
        btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        panelBotones.add(btnCancelar);
            
        // Agregar componentes al panel principal
        contentPanel.add(panelCampos, BorderLayout.CENTER);
        contentPanel.add(panelBotones, BorderLayout.SOUTH);
            
        add(contentPanel, BorderLayout.CENTER);
            
        // Cargar datos del producto si existe
        if (producto != null) {
            cargarDatosProducto();
        }
    }
    
    private void actualizarVisibilidadFechaVencimiento() {
        boolean visible = cmbTipo.getSelectedIndex() == 0; // Visible solo para productos perecederos
        lblFechaVencimiento.setVisible(visible);
        txtFechaVencimiento.setVisible(visible);
        pack();
    }
    
    private void guardarProducto() {
        try {
            // Validar campos obligatorios
            if (txtNombre.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "El nombre es obligatorio", 
                    "Error de validación", 
                    JOptionPane.ERROR_MESSAGE);
                txtNombre.requestFocus();
                return;
            }
            
            // Validar precio
            double precio;
            try {
                precio = Double.parseDouble(txtPrecio.getText().trim());
                if (precio <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "El precio debe ser un número mayor a cero", 
                    "Error de validación", 
                    JOptionPane.ERROR_MESSAGE);
                txtPrecio.requestFocus();
                return;
            }
            
            // Validar cantidad
            int cantidad;
            try {
                cantidad = Integer.parseInt(txtCantidad.getText().trim());
                if (cantidad < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "La cantidad debe ser un número entero no negativo", 
                    "Error de validación", 
                    JOptionPane.ERROR_MESSAGE);
                txtCantidad.requestFocus();
                return;
            }
            
            // Validar fecha de vencimiento si es producto perecedero
            String fechaVencimiento = null;
            if (cmbTipo.getSelectedIndex() == 0) { // Perecedero
                fechaVencimiento = txtFechaVencimiento.getText().trim();
                if (fechaVencimiento.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "La fecha de vencimiento es obligatoria para productos perecederos", 
                        "Error de validación", 
                        JOptionPane.ERROR_MESSAGE);
                    txtFechaVencimiento.requestFocus();
                    return;
                }
                
                try {
                    LocalDate.parse(fechaVencimiento, DATE_FORMATTER);
                } catch (DateTimeParseException e) {
                    JOptionPane.showMessageDialog(this, 
                        "El formato de la fecha de vencimiento debe ser dd/MM/yyyy", 
                        "Error de validación", 
                        JOptionPane.ERROR_MESSAGE);
                    txtFechaVencimiento.requestFocus();
                    return;
                }
            }
            
            // Obtener los datos del formulario
            String nombre = txtNombre.getText().trim();
            Impuesto impuesto = (Impuesto) cmbImpuesto.getSelectedItem();
            
            // Crear o actualizar el producto
            if (producto == null) {
                // Nuevo producto
                if (cmbTipo.getSelectedIndex() == 0) { // Perecedero
                    producto = new ProductoPerecedero(
                        nombre, 
                        "", // descripción vacía por ahora
                        precio, 
                        impuesto, 
                        cantidad, 
                        String.valueOf(System.currentTimeMillis()), // número de código temporal
                        fechaVencimiento
                    );
                    
                    // Guardar el nuevo producto perecedero
                    app.getProductoService().registrarProducto(producto);
                } else { // No perecedero
                    producto = new ProductoNoPerecedero(
                        nombre, 
                        "", // descripción vacía por ahora
                        precio, 
                        impuesto, 
                        cantidad, 
                        String.valueOf(System.currentTimeMillis()) // número de código temporal
                    );
                    
                    // Guardar el nuevo producto no perecedero
                    app.getProductoService().registrarProducto(producto);
                }
            } else {
                // Actualizar producto existente
                producto.setNombre(nombre);
                producto.setPrecio(precio);
                producto.setImpuesto(impuesto);
                
                if (producto instanceof ProductoPerecedero) {
                    ((ProductoPerecedero) producto).setFechaVencimiento(fechaVencimiento);
                }
                
                // Actualizar el producto
                app.getProductoService().actualizarProducto(producto);
            }
            
            JOptionPane.showMessageDialog(this, 
                "Producto " + (producto.getCodigo() == 0 ? "guardado" : "actualizado") + " correctamente.",
                "Operación exitosa",
                JOptionPane.INFORMATION_MESSAGE);
                
            guardado = true;
            dispose();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error al guardar el producto: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
