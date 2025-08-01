package com.facturacion.model;

/**
 * Clase que representa un cliente en el sistema.
 * Puede ser de tipo OCASIONALES o CORPORATIVOS.
 * Los clientes ocasionales pueden ser Ciudadano de Oro para aplicar descuentos.
 */
public class Cliente {
    private long cedula;
    private String nombre;
    private TipoCliente tipo;
    private boolean ciudadanoOro;
    private int puntos;
    private String contacto; // Solo para clientes corporativos

    /**
     * Constructor para crear un nuevo cliente.
     * 
     * @param cedula Número de cédula del cliente
     * @param nombre Nombre completo del cliente
     * @param tipo Tipo de cliente (OCASIONALES o CORPORATIVOS)
     * @param contacto Información de contacto (obligatorio para clientes corporativos)
     * @throws IllegalArgumentException Si los datos no son válidos
     */
    public Cliente(long cedula, String nombre, TipoCliente tipo, String contacto) {
        if (cedula <= 0) {
            throw new IllegalArgumentException("La cédula debe ser un número positivo");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de cliente es obligatorio");
        }
        if (tipo == TipoCliente.CORPORATIVOS && (contacto == null || contacto.trim().isEmpty())) {
            throw new IllegalArgumentException("El contacto es obligatorio para clientes corporativos");
        }
        
        this.cedula = cedula;
        this.nombre = nombre.trim();
        this.tipo = tipo;
        this.ciudadanoOro = false;
        this.puntos = 0;
        this.contacto = tipo == TipoCliente.CORPORATIVOS ? contacto.trim() : null;
    }

    // Getters y Setters
    public long getCedula() {
        return cedula;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        this.nombre = nombre.trim();
    }

    public TipoCliente getTipo() {
        return tipo;
    }

    public void setTipo(TipoCliente tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de cliente no puede ser nulo");
        }
        this.tipo = tipo;
        
        // Si se cambia a tipo OCASIONALES, se elimina el contacto
        if (tipo == TipoCliente.OCASIONALES) {
            this.contacto = null;
        }
    }

    public boolean isCiudadanoOro() {
        return ciudadanoOro;
    }

    /**
     * Establece si el cliente es Ciudadano de Oro.
     * Solo aplicable para clientes ocasionales.
     * 
     * @param ciudadanoOro true si es Ciudadano de Oro, false en caso contrario
     * @throws IllegalStateException Si se intenta establecer a true para un cliente corporativo
     */
    public void setCiudadanoOro(boolean ciudadanoOro) {
        if (ciudadanoOro && tipo == TipoCliente.CORPORATIVOS) {
            throw new IllegalStateException("Los clientes corporativos no pueden ser Ciudadano de Oro");
        }
        this.ciudadanoOro = ciudadanoOro;
    }

    public int getPuntos() {
        return puntos;
    }

    /**
     * Agrega puntos al cliente.
     * 
     * @param puntos Puntos a agregar (debe ser un número positivo)
     * @return La nueva cantidad de puntos
     * @throws IllegalArgumentException Si los puntos son negativos
     */
    public int agregarPuntos(int puntos) {
        if (puntos < 0) {
            throw new IllegalArgumentException("No se pueden agregar puntos negativos");
        }
        this.puntos += puntos;
        return this.puntos;
    }

    /**
     * Canjea puntos del cliente.
     * 
     * @param puntos Puntos a canjear (debe ser un número positivo)
     * @return true si se pudieron canjear los puntos, false si no hay suficientes
     * @throws IllegalArgumentException Si los puntos son negativos
     */
    public boolean canjearPuntos(int puntos) {
        if (puntos < 0) {
            throw new IllegalArgumentException("No se pueden canjear puntos negativos");
        }
        if (this.puntos >= puntos) {
            this.puntos -= puntos;
            return true;
        }
        return false;
    }

    public String getContacto() {
        return contacto;
    }

    /**
     * Establece la información de contacto del cliente.
     * Solo aplicable para clientes corporativos.
     * 
     * @param contacto Información de contacto
     * @throws IllegalStateException Si se intenta establecer para un cliente ocasional
     * @throws IllegalArgumentException Si el contacto es nulo o vacío
     */
    public void setContacto(String contacto) {
        if (tipo == TipoCliente.OCASIONALES) {
            throw new IllegalStateException("Los clientes ocasionales no tienen contacto");
        }
        if (contacto == null || contacto.trim().isEmpty()) {
            throw new IllegalArgumentException("El contacto es obligatorio para clientes corporativos");
        }
        this.contacto = contacto.trim();
    }

    @Override
    public String toString() {
        return String.format("%s (Cédula: %d, Tipo: %s%s%s)", 
            nombre, 
            cedula, 
            tipo,
            tipo == TipoCliente.CORPORATIVOS ? ", Contacto: " + contacto : "",
            ciudadanoOro ? ", Ciudadano de Oro" : "");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Cliente other = (Cliente) obj;
        return cedula == other.cedula;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(cedula);
    }
}
