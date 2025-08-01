package com.facturacion.model;

public class TestFactura {
    public static void main(String[] args) {
        // Test if we can create a Factura instance
        Cliente cliente = new Cliente(1, "Test Cliente", TipoCliente.OCASIONAL);
        Factura factura = new Factura(cliente);
        System.out.println("Factura creada exitosamente: " + factura);
    }
}
