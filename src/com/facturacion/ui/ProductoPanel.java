package com.facturacion.ui;

import com.facturacion.App;
import com.facturacion.model.Impuesto;
import com.facturacion.model.Producto;
import com.facturacion.model.ProductoNoPerecedero;
import com.facturacion.model.ProductoPerecedero;
import com.facturacion.service.ProductoService;
import com.facturacion.service.impl.ProductoServiceImpl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * Panel para el mantenimiento de productos.
 * Permite listar, agregar, modificar y eliminar productos.
 */
public class ProductoPanel extends JPanel {

    private final App app;
    private JTable tablaProductos;
    private JTextField txtFiltro;
    private JButton btnAgregar;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnActualizar;
    private JComboBox<Impuesto> cmbImpuesto;
    private DefaultTableModel tableModel;

    // Formato de fecha para la interfaz de usuario
    // Formato de fecha para mostrar en la interfaz
    // Formato de fecha para la fecha de vencimiento (usado en validación)
    @SuppressWarnings("unused")
    private static final String DATE_FORMAT = "dd/MM/yyyy";

    // Servicio
    private final ProductoService productoService;

    /**
     * Crea una nueva instancia del panel de productos.
     *
     * @param app La aplicación principal
     */
    public ProductoPanel(App app) {
        this.app = app;
        this.productoService = new ProductoServiceImpl(null); // Repository will be injected properly later
        initUIComponents();
        configurarTabla();
        cargarProductos();
        cargarImpuestos();
    }

    private void initUIComponents() {
        // Componentes de la interfaz
        setLayout(new BorderLayout());

        // Panel superior con filtros
        JPanel panelFiltros = new JPanel();
        panelFiltros.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel lblFiltro = new JLabel("Filtrar por nombre:");
        txtFiltro = new JTextField(20);
        JButton btnFiltrar = new JButton("Filtrar");

        btnFiltrar.addActionListener(e -> filtrarProductos());
        txtFiltro.addActionListener(e -> filtrarProductos());

        panelFiltros.add(lblFiltro);
        panelFiltros.add(txtFiltro);
        panelFiltros.add(btnFiltrar);

        // Panel de botones
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new FlowLayout(FlowLayout.LEFT));
        btnAgregar = new JButton("Agregar Producto");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
        btnActualizar = new JButton("Actualizar");

        // Configurar acciones de los botones
        ActionListener buttonListener = e -> {
            if (e.getSource() == btnAgregar) {
                mostrarDialogoNuevoProducto();
            } else if (e.getSource() == btnEditar) {
                editarProductoSeleccionado();
            } else if (e.getSource() == btnEliminar) {
                eliminarProductoSeleccionado();
            } else if (e.getSource() == btnActualizar) {
                cargarProductos();
            }
        };
        
        btnAgregar.addActionListener(e -> mostrarDialogoNuevoProducto());
        btnEditar.addActionListener(e -> editarProductoSeleccionado());
        btnEliminar.addActionListener(e -> eliminarProductoSeleccionado());
        btnActualizar.addActionListener(buttonListener);

        panelBotones.add(btnAgregar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnActualizar);

        // Tabla de productos
        tableModel = new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Código", "Nombre", "Precio", "Impuesto", "Cantidad", "Tipo", "Fecha Vencimiento"
            }
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer que la tabla no sea editable
            }
        };

        tablaProductos = new JTable(tableModel);
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.getTableHeader().setReorderingAllowed(false);

        // Hacer que la tabla sea seleccionable con el teclado
        tablaProductos.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    editarProductoSeleccionado();
                } else if (event.getKeyCode() == KeyEvent.VK_DELETE) {
                    eliminarProductoSeleccionado();
                }
            }
        });
        
        txtFiltro.getDocument().addDocumentListener(new DocumentListener() {
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

        // Agregar componentes al panel principal
        add(panelFiltros, BorderLayout.NORTH);
        add(new JScrollPane(tablaProductos), BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void configurarTabla() {
        // Configurar el ordenamiento de la tabla
        tablaProductos.setAutoCreateRowSorter(true);
        if (tableModel != null) {
            tablaProductos.setRowSorter(new TableRowSorter<>(tableModel));
        }

        // Configurar la selección de filas en la tabla
        tablaProductos.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                boolean filaSeleccionada = tablaProductos.getSelectedRow() != -1;
                btnEditar.setEnabled(filaSeleccionada);
                btnEliminar.setEnabled(filaSeleccionada);
            }
        });
    }

    // Carga los productos desde el servicio y los muestra en la tabla.
    private void cargarProductos() {
        // Limpiar la tabla
        DefaultTableModel model = (DefaultTableModel) tablaProductos.getModel();
        model.setRowCount(0);

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
                    // Format the date as a string using the formatter
                    String fechaVencimiento = pp.getFechaVencimiento();
                    fila[6] = fechaVencimiento != null ? fechaVencimiento : "";
                } else {
                    fila[5] = "No Perecedero";
                    fila[6] = "";
                }

                model.addRow(fila);
            }

            // Ordenar por código de producto por defecto
            if (tablaProductos.getRowSorter() != null && tablaProductos.getRowSorter().getSortKeys() != null && !tablaProductos.getRowSorter().getSortKeys().isEmpty()) {
                tablaProductos.getRowSorter().toggleSortOrder(0);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar los productos: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarImpuestos() {
        // Initialize the combo box with impuesto values
        cmbImpuesto.removeAllItems();
        for (Impuesto impuesto : Impuesto.values()) {
            cmbImpuesto.addItem(impuesto);
        }
    }

    private void filtrarProductos() {
        if (txtFiltro == null || tablaProductos == null) return;
        
        String textoBusqueda = txtFiltro.getText().toLowerCase();
        
        RowFilter<TableModel, Object> filtro = RowFilter.regexFilter("(?i)" + textoBusqueda);

        if (tablaProductos.getRowSorter() != null) {
            @SuppressWarnings("unchecked")
            TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) tablaProductos.getRowSorter();
            if (sorter != null) {
                sorter.setRowFilter(filtro);
            }
        }
    }

    /**
     * Muestra el diálogo para agregar un nuevo producto.
     */
    private void mostrarDialogoNuevoProducto() {
        JDialogProducto dialog = new JDialogProducto(SwingUtilities.getWindowAncestor((Component)this), null);
        dialog.setVisible(true);
        if (dialog.isGuardado()) {
            cargarProductos();
        }
    }

    /**
     * Muestra el diálogo para editar el producto seleccionado.
     */
    private void editarProductoSeleccionado() {
        int selectedRow = tablaProductos.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione un producto para editar.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int modelRow = tablaProductos.convertRowIndexToModel(selectedRow);
        int codigo = (int) tablaProductos.getModel().getValueAt(modelRow, 0);
        
        try {
            // Use the local productoService field instead of app.getProductoService()
            Producto producto = productoService.buscarProductoPorId(codigo);
            if (producto != null) {
                JDialogProducto dialog = new JDialogProducto(SwingUtilities.getWindowAncestor((Component)this), producto);
                dialog.setVisible(true);
                if (dialog.isGuardado()) {
                    cargarProductos();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar el producto: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Elimina el producto seleccionado de la base de datos.
     */
    private void eliminarProductoSeleccionado() {
        int selectedRow = tablaProductos.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione un producto para eliminar.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de que desea eliminar el producto seleccionado?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            int modelRow = tablaProductos.convertRowIndexToModel(selectedRow);
            int codigo = (int) tablaProductos.getModel().getValueAt(modelRow, 0);
            
            try {
                productoService.eliminarProducto(codigo);
                cargarProductos();
                JOptionPane.showMessageDialog(this,
                    "Producto eliminado correctamente.",
                    "Operación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error al eliminar el producto: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    protected void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private final class JDialogProducto extends JDialog {
        public boolean isGuardado() {
            return guardado;
        }

        private Producto producto;  // Removed final to allow reassignment in guardarProducto()
        private boolean guardado = false;
        // DATE_FORMAT is used for date parsing/formatting consistency

        // Componentes de la interfaz
        private final JTextField txtCodigo = new JTextField(15);
        private final JTextField txtNombre = new JTextField(15);
        private final JTextField txtPrecio = new JTextField(15);
        private final JComboBox<Impuesto> cmbImpuesto = new JComboBox<>();
        private final JTextField txtCantidad = new JTextField(15);
        private final JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"Perecedero", "No perecedero"});
        private final JTextField txtFechaVencimiento = new JTextField(15);
        private final JButton btnGuardar = new JButton("Guardar");
        private final JButton btnCancelar = new JButton("Cancelar");
        private final JLabel lblFechaVencimiento = new JLabel("Fecha Vencimiento (dd/MM/yyyy):");
        private final JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        private final JPanel panelCampos = new JPanel(new GridBagLayout());
        private final JPanel panelBotonesDialogo;
        
        {
            panelBotonesDialogo = new JPanel();
            panelBotonesDialogo.setLayout(new FlowLayout(FlowLayout.RIGHT));
        }

        public JDialogProducto(Window owner, Producto producto) {
            super(owner, producto == null ? "Nuevo Producto" : "Editar Producto", Dialog.ModalityType.APPLICATION_MODAL);
            this.producto = producto;
            initComponents();
            cargarDatosProducto();
            pack();
            setLocationRelativeTo(owner);
        }

        private void initComponents() {
            setLayout(new BorderLayout(10, 10));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            // Configurar GridBagConstraints
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            // Código
            gbc.gridx = 0;
            gbc.gridy = 0;
            panelCampos.add(new JLabel("Código:"), gbc);
            
            gbc.gridx = 1;
            panelCampos.add(txtCodigo, gbc);
            
            // Nombre
            gbc.gridx = 0;
            gbc.gridy++;
            panelCampos.add(new JLabel("Nombre:"), gbc);
            
            gbc.gridx = 1;
            panelCampos.add(txtNombre, gbc);
            
            // Precio
            gbc.gridx = 0;
            gbc.gridy++;
            panelCampos.add(new JLabel("Precio:"), gbc);
            
            gbc.gridx = 1;
            panelCampos.add(txtPrecio, gbc);
            
            // Impuesto
            gbc.gridx = 0;
            gbc.gridy++;
            panelCampos.add(new JLabel("Impuesto:"), gbc);
            
            gbc.gridx = 1;
            // Llenar combo de impuestos
            cmbImpuesto.removeAllItems();
            for (Impuesto impuesto : Impuesto.values()) {
                cmbImpuesto.addItem(impuesto);
            }
            panelCampos.add(cmbImpuesto, gbc);
            
            // Cantidad
            gbc.gridx = 0;
            gbc.gridy++;
            panelCampos.add(new JLabel("Cantidad:"), gbc);
            
            gbc.gridx = 1;
            panelCampos.add(txtCantidad, gbc);
            
            // Tipo de producto
            gbc.gridx = 0;
            gbc.gridy++;
            panelCampos.add(new JLabel("Tipo:"), gbc);
            
            gbc.gridx = 1;
            cmbTipo.addActionListener(_ -> actualizarVisibilidadFechaVencimiento());
            panelCampos.add(cmbTipo, gbc);
            
            // Fecha de vencimiento (inicialmente oculta)
            gbc.gridx = 0;
            gbc.gridy++;
            panelCampos.add(lblFechaVencimiento, gbc);
            
            gbc.gridx = 1;
            panelCampos.add(txtFechaVencimiento, gbc);
            
            // Botones
            btnGuardar.addActionListener(_ -> guardarProducto());
            btnCancelar.addActionListener(_ -> dispose());
            
            panelBotonesDialogo.add(btnGuardar);
            panelBotonesDialogo.add(btnCancelar);
            
            // Agregar paneles al contenido
            contentPanel.add(panelCampos, BorderLayout.CENTER);
            contentPanel.add(panelBotonesDialogo, BorderLayout.SOUTH);
            add(contentPanel, BorderLayout.CENTER);
            
            // Actualizar visibilidad de campos según el tipo de producto
            actualizarVisibilidadFechaVencimiento();
            
            // Configurar el diálogo
            setResizable(false);
            getRootPane().setDefaultButton(btnGuardar);
        }

        private void cargarDatosProducto() {
            if (producto == null) {
                // Valores por defecto para nuevo producto
                txtCodigo.setText("");
                txtNombre.setText("");
                txtPrecio.setText("");
                txtCantidad.setText("0");
                cmbTipo.setSelectedIndex(1); // No perecedero por defecto
                txtFechaVencimiento.setText("");
                if (cmbImpuesto.getItemCount() > 0) {
                    cmbImpuesto.setSelectedIndex(0);
                }
                return;
            }

            txtCodigo.setText(String.valueOf(producto.getCodigo()));
            txtNombre.setText(producto.getNombre());
            txtPrecio.setText(String.format("%.2f", producto.getPrecio()));

            if (producto.getImpuesto() != null) {
                cmbImpuesto.setSelectedItem(producto.getImpuesto());
            }

            txtCantidad.setText(String.valueOf(producto.getCantidadProducto()));

            // Configurar el tipo de producto y fecha de vencimiento si es perecedero
            if (producto instanceof ProductoPerecedero) {
                cmbTipo.setSelectedIndex(0); // Seleccionar "Perecedero"
                ProductoPerecedero pp = (ProductoPerecedero) producto;
                if (pp.getFechaVencimiento() != null) {
                    txtFechaVencimiento.setText(pp.getFechaVencimiento());
                }
            } else {
                cmbTipo.setSelectedIndex(1); // Seleccionar "No perecedero"
                txtFechaVencimiento.setText("");
            }
        }

        private void actualizarVisibilidadFechaVencimiento() {
            boolean esPerecedero = cmbTipo.getSelectedIndex() == 0;
            lblFechaVencimiento.setVisible(esPerecedero);
            txtFechaVencimiento.setVisible(esPerecedero);
        }

        private void guardarProducto() {
            try {
                // Validar fecha de vencimiento si es perecedero
                if (cmbTipo.getSelectedIndex() == 0) {
                    String fechaVencimiento = txtFechaVencimiento.getText().trim();
                    if (fechaVencimiento.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "La fecha de vencimiento es requerida para productos perecederos", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    // Validar formato de fecha simple (dd/MM/yyyy)
                    if (!fechaVencimiento.matches("^\\d{2}/\\d{2}/\\d{4}$")) {
                        JOptionPane.showMessageDialog(this, "El formato de fecha debe ser dd/MM/yyyy", "Error", JOptionPane.ERROR_MESSAGE);
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
                                "",
                                Double.parseDouble(txtPrecio.getText()),
                                impuesto,
                                Integer.parseInt(txtCantidad.getText()),
                                String.valueOf(System.currentTimeMillis()),
                                txtFechaVencimiento.getText()
                        );

                        // Guardar el nuevo producto perecedero
                        app.getProductoService().registrarProducto(producto);
                    } else { // No perecedero
                        producto = new ProductoNoPerecedero(
                                nombre,
                                "",
                                Double.parseDouble(txtPrecio.getText()),
                                impuesto,
                                Integer.parseInt(txtCantidad.getText()),
                                String.valueOf(System.currentTimeMillis())
                        );

                        // Guardar el nuevo producto no perecedero
                        app.getProductoService().registrarProducto(producto);
                    }
                } else {
                    // Actualizar producto existente
                    producto.setNombre(nombre);
                    producto.setPrecio(Double.parseDouble(txtPrecio.getText()));
                    producto.setImpuesto(impuesto);

                    if (producto instanceof ProductoPerecedero) {
                        ((ProductoPerecedero) producto).setFechaVencimiento(txtFechaVencimiento.getText());
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

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(ProductoPanel.this,
                        "Error al guardar el producto: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }
}
