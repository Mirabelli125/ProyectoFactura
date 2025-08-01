package com.facturacion.ui;

import com.facturacion.App;
import com.facturacion.model.Impuesto;
import com.facturacion.model.Producto;
import com.facturacion.model.ProductoNoPerecedero;
import com.facturacion.model.ProductoPerecedero;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import javax.swing.RowFilter;
import java.util.Map.Entry;
import javax.persistence.PersistenceException;
import javax.swing.JDialog;
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
 * Panel para el mantenimiento de productos.
 * Permite listar, agregar, modificar y eliminar productos.
 */
public class ProductoPanel extends BasePanel {
    
    // Componentes de la interfaz
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JButton btnNuevo;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JComboBox<String> cmbTipoProducto;
    private JComboBox<Impuesto> cmbImpuesto;
    
    // Filtro para la tabla
    private TableRowSorter<TableModel> sorter;
    
    // Formateador de fechas
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    /**
     * Crea una nueva instancia del panel de productos.
     * 
     * @param app Referencia a la aplicación principal
     */
    public ProductoPanel(App app) {
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
        
        cmbTipoProducto = new JComboBox<>(new String[]{"Todos", "Perecedero", "No perecedero"});
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        panelBusqueda.add(cmbTipoProducto, gbc);
        
        // Panel de la tabla
        JPanel panelTabla = new JPanel(new GridBagLayout());
        panelTabla.setBorder(javax.swing.BorderFactory.createTitledBorder("Productos"));
        
        // Crear la tabla de productos
        modeloTabla = new DefaultTableModel(
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
        
        btnNuevo = new JButton("Nuevo");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
        
        btnNuevo.addActionListener(e -> mostrarDialogoProducto(null));
        btnEditar.addActionListener(e -> editarProductoSeleccionado());
        btnEliminar.addActionListener(e -> eliminarProductoSeleccionado());
        
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
        cargarProductos();
    }
    
    /**
     * Configura el filtro de búsqueda para la tabla de productos.
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
        
        // Filtrar por tipo de producto
        cmbTipoProducto.addActionListener(e -> filtrarTabla());
    }
    
    /**
     * Aplica los filtros de búsqueda a la tabla.
     */
    private void filtrarTabla() {
        String texto = txtBuscar.getText().toLowerCase();
        String tipoSeleccionado = (String) cmbTipoProducto.getSelectedItem();
        
        sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                // Filtrar por texto de búsqueda (código, nombre)
                String codigo = entry.getStringValue(0).toLowerCase();
                String nombre = entry.getStringValue(1).toLowerCase();
                String tipo = entry.getStringValue(5);
                
                boolean coincideTexto = codigo.contains(texto) || nombre.contains(texto);
                
                // Filtrar por tipo de producto
                boolean coincideTipo = tipoSeleccionado.equals("Todos") ||
                    (tipoSeleccionado.equals("Perecedero") && tipo.equals("Perecedero")) ||
                    (tipoSeleccionado.equals("No perecedero") && tipo.equals("No perecedero"));
                
                return coincideTexto && coincideTipo;
            }
        });
    }
    
    /**
     * Carga los productos desde el servicio y los muestra en la tabla.
     */
    private void cargarProductos() {
        // Limpiar la tabla
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
                    fila[6] = pp.getFechaVencimiento().format(dateFormatter);
                } else {
                    fila[5] = "No perecedero";
                    fila[6] = "N/A";
                }
                
                modeloTabla.addRow(fila);
            }
            
            // Ordenar por código de producto por defecto
            tablaProductos.getRowSorter().toggleSortOrder(0);
            
        } catch (Exception e) {
            mostrarError("Error al cargar los productos: " + e.getMessage());
        }
    }
    
    /**
     * Muestra el diálogo para agregar o editar un producto.
     * 
     * @param producto Producto a editar, o null para crear uno nuevo
     */
    private void mostrarDialogoProducto(Producto producto) {
        // Crear el diálogo
        JDialogProducto dialogo = new JDialogProducto(this, producto);
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
        
        // Si se guardaron los cambios, actualizar la tabla
        if (dialogo.isGuardado()) {
            cargarProductos();
        }
    }
    
    /**
     * Obtiene el producto seleccionado en la tabla.
     * 
     * @return El producto seleccionado, o null si no hay selección
     */
    private Producto obtenerProductoSeleccionado() {
        int filaSeleccionada = tablaProductos.getSelectedRow();
        if (filaSeleccionada == -1) {
            return null;
        }
        
        // Obtener el código del producto seleccionado
        int codigo = (int) modeloTabla.getValueAt(
            tablaProductos.convertRowIndexToModel(filaSeleccionada), 0);
        
        // Buscar el producto en el servicio
        return app.getProductoService().buscarPorCodigo(codigo).orElse(null);
    }
    
    /**
     * Edita el producto seleccionado en la tabla.
     */
    private void editarProductoSeleccionado() {
        Producto producto = obtenerProductoSeleccionado();
        if (producto == null) {
            mostrarError("Por favor, seleccione un producto para editar.");
            return;
        }
        
        mostrarDialogoProducto(producto);
    }
    
    /**
     * Elimina el producto seleccionado de la tabla.
     */
    private void eliminarProductoSeleccionado() {
        Producto producto = obtenerProductoSeleccionado();
        if (producto == null) {
            mostrarError("Por favor, seleccione un producto para eliminar.");
            return;
        }
        
        // Confirmar la eliminación
        boolean confirmar = confirmar(
            "¿Está seguro que desea eliminar el producto " + producto.getNombre() + "?");
        
        if (confirmar) {
            try {
                // Eliminar el producto
                boolean eliminado = app.getProductoService().eliminarProducto(producto.getCodigo());
                
                if (eliminado) {
                    mostrarInformacion("Producto eliminado correctamente.");
                    cargarProductos();
                } else {
                    mostrarError("No se pudo eliminar el producto.");
                }
            } catch (Exception e) {
                mostrarError("Error al eliminar el producto: " + e.getMessage());
            }
        }
    }
    
    /**
     * Clase interna para el diálogo de edición de productos.
     */
    private class JDialogProducto extends javax.swing.JDialog {
        
        private final ProductoPanel panelPadre;
        private final Producto producto;
        private boolean guardado = false;
        
        // Componentes del formulario
        private JTextField txtCodigo;
        private JTextField txtNombre;
        private JTextField txtPrecio;
        private JComboBox<Impuesto> cmbImpuesto;
        private JTextField txtCantidad;
        private JComboBox<String> cmbTipo;
        private JTextField txtFechaVencimiento;
        private JButton btnGuardar;
        private JButton btnCancelar;
        
        /**
         * Crea un nuevo diálogo para agregar o editar un producto.
         * 
         * @param panelPadre Panel padre que crea el diálogo
         * @param producto Producto a editar, o null para crear uno nuevo
         */
        public JDialogProducto(ProductoPanel panelPadre, Producto producto) {
            super(panelPadre, producto == null ? "Nuevo Producto" : "Editar Producto", true);
            this.panelPadre = panelPadre;
            this.producto = producto;
            
            initComponents();
            pack();
            setLocationRelativeTo(panelPadre);
        }
        
        /**
         * Inicializa los componentes del diálogo.
         */
        private void initComponents() {
            setLayout(new java.awt.GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            
            // Código
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(new JLabel("Código:"), gbc);
            
            txtCodigo = new JTextField(15);
            txtCodigo.setEditable(false);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(txtCodigo, gbc);
            
            // Nombre
            gbc.gridx = 0;
            gbc.gridy++;
            add(new JLabel("Nombre:"), gbc);
            
            txtNombre = new JTextField(30);
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(txtNombre, gbc);
            
            // Precio
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 1;
            add(new JLabel("Precio:"), gbc);
            
            txtPrecio = new JTextField(10);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(txtPrecio, gbc);
            
            // Impuesto
            gbc.gridx = 2;
            add(new JLabel("Impuesto:"), gbc);
            
            cmbImpuesto = new JComboBox<>(Impuesto.values());
            gbc.gridx = 3;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(cmbImpuesto, gbc);
            
            // Cantidad
            gbc.gridx = 0;
            gbc.gridy++;
            add(new JLabel("Cantidad:"), gbc);
            
            txtCantidad = new JTextField(10);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(txtCantidad, gbc);
            
            // Tipo de producto
            gbc.gridx = 2;
            add(new JLabel("Tipo:"), gbc);
            
            cmbTipo = new JComboBox<>(new String[]{"Perecedero", "No perecedero"});
            cmbTipo.addActionListener(e -> actualizarCamposTipo());
            gbc.gridx = 3;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(cmbTipo, gbc);
            
            // Fecha de vencimiento (inicialmente oculto)
            JLabel lblFechaVencimiento = new JLabel("Vencimiento (dd/mm/aaaa):");
            gbc.gridx = 0;
            gbc.gridy++;
            add(lblFechaVencimiento, gbc);
            
            txtFechaVencimiento = new JTextField(10);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(txtFechaVencimiento, gbc);
            
            // Panel de botones
            JPanel panelBotones = new JPanel();
            btnGuardar = new JButton("Guardar");
            btnCancelar = new JButton("Cancelar");
            
            btnGuardar.addActionListener(e -> guardarProducto());
            btnCancelar.addActionListener(e -> dispose());
            
            panelBotones.add(btnGuardar);
            panelBotones.add(btnCancelar);
            
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.gridwidth = 4;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.fill = GridBagConstraints.NONE;
            add(panelBotones, gbc);
            
            // Cargar los datos del producto si se está editando
            if (producto != null) {
                cargarDatosProducto();
            } else {
                // Nuevo producto
                txtCodigo.setText("(Generado automáticamente)");
                txtCantidad.setText("0");
                cmbTipo.setSelectedIndex(0);
                actualizarCamposTipo();
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
         * Actualiza los campos del formulario según el tipo de producto seleccionado.
         */
        private void actualizarCamposTipo() {
            boolean esPerecedero = cmbTipo.getSelectedIndex() == 0;
            txtFechaVencimiento.setEnabled(esPerecedero);
            
            if (!esPerecedero) {
                txtFechaVencimiento.setText("");
            } else if (producto instanceof ProductoPerecedero) {
                txtFechaVencimiento.setText(
                    ((ProductoPerecedero) producto).getFechaVencimiento().format(dateFormatter));
            }
        }
        
        /**
         * Carga los datos del producto en el formulario.
         */
        private void cargarDatosProducto() {
            txtCodigo.setText(String.valueOf(producto.getCodigo()));
            txtNombre.setText(producto.getNombre());
            txtPrecio.setText(String.format("%.2f", producto.getPrecio()));
            cmbImpuesto.setSelectedItem(producto.getImpuesto());
            txtCantidad.setText(String.valueOf(producto.getCantidadDisponible()));
            
            if (producto instanceof ProductoPerecedero) {
                cmbTipo.setSelectedIndex(0);
                txtFechaVencimiento.setText(
                    ((ProductoPerecedero) producto).getFechaVencimiento().format(dateFormatter));
            } else {
                cmbTipo.setSelectedIndex(1);
            }
            
            actualizarCamposTipo();
        }
        
        /**
         * Valida los datos del formulario.
         * 
         * @return true si los datos son válidos, false en caso contrario
         */
        private boolean validarDatos() {
            // Validar nombre
            String nombre = txtNombre.getText().trim();
            if (nombre.isEmpty()) {
                mostrarError("El nombre es obligatorio.");
                txtNombre.requestFocus();
                return false;
            }
            
            // Validar que el nombre solo contenga letras, números y espacios
            if (!nombre.matches("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s]+")) {
                mostrarError("El nombre solo puede contener letras, números y espacios.");
                txtNombre.requestFocus();
                return false;
            }
            
            // Validar que el nombre no exceda los 100 caracteres
            if (nombre.length() > 100) {
                mostrarError("El nombre no puede tener más de 100 caracteres.");
                txtNombre.requestFocus();
                return false;
            }
            
            // Validar precio
            try {
                double precio = Double.parseDouble(txtPrecio.getText().trim());
                if (precio <= 0) {
                    mostrarError("El precio debe ser mayor que cero.");
                    txtPrecio.requestFocus();
                    return false;
                }
                
                // Validar que el precio no sea excesivamente alto
                if (precio > 1_000_000.00) {
                    mostrarError("El precio no puede ser mayor a ₡1,000,000.00");
                    txtPrecio.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                mostrarError("El precio debe ser un número válido (ejemplo: 1500.50).");
                txtPrecio.requestFocus();
                return false;
            }
            
            // Validar cantidad
            try {
                int cantidad = Integer.parseInt(txtCantidad.getText().trim());
                if (cantidad < 0) {
                    mostrarError("La cantidad no puede ser negativa.");
                    txtCantidad.requestFocus();
                    return false;
                }
                
                // Validar que la cantidad no sea excesivamente grande
                if (cantidad > 100_000) {
                    mostrarError("La cantidad no puede ser mayor a 100,000 unidades.");
                    txtCantidad.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                mostrarError("La cantidad debe ser un número entero válido (ejemplo: 10).");
                txtCantidad.requestFocus();
                return false;
            }
            
            // Validar fecha de vencimiento para productos perecederos
            if (cmbTipo.getSelectedIndex() == 0) { // Perecedero
                try {
                    String fechaStr = txtFechaVencimiento.getText().trim();
                    if (fechaStr.isEmpty()) {
                        mostrarError("La fecha de vencimiento es obligatoria para productos perecederos.");
                        txtFechaVencimiento.requestFocus();
                        return false;
                    }
                    
                    // Intentar parsear la fecha con el formato especificado
                    LocalDate fechaVencimiento = LocalDate.parse(fechaStr, dateFormatter);
                    LocalDate hoy = LocalDate.now();
                    
                    if (fechaVencimiento.isBefore(hoy)) {
                        mostrarError("La fecha de vencimiento no puede ser anterior a la fecha actual (" + 
                            hoy.format(dateFormatter) + ").");
                        txtFechaVencimiento.requestFocus();
                        return false;
                    }
                    
                    // Validar que la fecha no sea demasiado lejana en el futuro (10 años)
                    LocalDate fechaMaxima = hoy.plusYears(10);
                    if (fechaVencimiento.isAfter(fechaMaxima)) {
                        mostrarError("La fecha de vencimiento no puede ser posterior a " + 
                            fechaMaxima.format(dateFormatter) + ".");
                        txtFechaVencimiento.requestFocus();
                        return false;
                    }
                } catch (DateTimeParseException e) {
                    mostrarError("El formato de la fecha debe ser dd/mm/aaaa (ejemplo: 31/12/2023).");
                    txtFechaVencimiento.requestFocus();
                    return false;
                }
            }
            
            return true;
        }
        
        /**
         * Prepara el objeto Producto con los datos del formulario.
         * 
         * @return Producto listo para ser guardado
         * @throws NumberFormatException si hay un error al convertir los valores numéricos
         * @throws DateTimeParseException si hay un error al parsear la fecha de vencimiento
         */
        private Producto prepararProductoParaGuardar() throws NumberFormatException, DateTimeParseException {
            Producto productoGuardar;
            boolean esNuevo = (producto == null);
            String nombre = txtNombre.getText().trim();
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            Impuesto impuesto = (Impuesto) cmbImpuesto.getSelectedItem();
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            boolean esPerecedero = cmbTipo.getSelectedIndex() == 0;
            
            if (esNuevo) {
                if (esPerecedero) {
                    ProductoPerecedero pp = new ProductoPerecedero();
                    pp.setFechaVencimiento(LocalDate.parse(txtFechaVencimiento.getText().trim(), dateFormatter));
                    productoGuardar = pp;
                } else {
                    productoGuardar = new ProductoNoPerecedero();
                }
            } else {
                productoGuardar = producto;
                
                // Actualizar la fecha de vencimiento si es perecedero
                if (productoGuardar instanceof ProductoPerecedero && esPerecedero) {
                    ((ProductoPerecedero) productoGuardar).setFechaVencimiento(
                        LocalDate.parse(txtFechaVencimiento.getText().trim(), dateFormatter)
                    );
                }
            }
            
            // Establecer los valores comunes
            productoGuardar.setNombre(nombre);
            productoGuardar.setPrecio(precio);
            productoGuardar.setImpuesto(impuesto);
            productoGuardar.setCantidadDisponible(cantidad);
            
            return productoGuardar;
        }
        
        /**
         * Guarda el producto con los datos del formulario.
         */
        private void guardarProducto() {
            if (!validarDatos()) {
                return;
            }
            
            try {
                // Preparar el producto con los datos del formulario
                Producto productoGuardar = prepararProductoParaGuardar();
                boolean esNuevo = (producto == null);
                
                // Guardar el producto
                if (esNuevo) {
                    productoGuardar = app.getProductoService().registrarProducto(productoGuardar);
                    JOptionPane.showMessageDialog(this, 
                        "Producto registrado correctamente con código: " + productoGuardar.getCodigo(),
                        "Registro exitoso",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    productoGuardar = app.getProductoService().actualizarProducto(productoGuardar);
                    JOptionPane.showMessageDialog(this, 
                        "Producto actualizado correctamente.",
                        "Actualización exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
                }
                
                guardado = true;
                dispose();
                
            } catch (NumberFormatException e) {
                mostrarError("Error en el formato de los datos numéricos: " + e.getMessage());
            } catch (DateTimeParseException e) {
                mostrarError("Error en el formato de la fecha de vencimiento: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                mostrarError("Error de validación: " + e.getMessage());
            } catch (javax.persistence.PersistenceException e) {
                if (e.getCause() != null && e.getCause().getMessage().contains("duplicate key")) {
                    mostrarError("Ya existe un producto con el mismo nombre.");
                } else {
                    mostrarError("Error al guardar el producto en la base de datos: " + e.getMessage());
                }
            } catch (Exception e) {
                mostrarError("Error inesperado al guardar el producto: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        /**
         * Indica si se guardaron los cambios en el producto.
         * 
         * @return true si se guardaron los cambios, false en caso contrario
         */
        public boolean isGuardado() {
            return guardado;
        }
    }
}
