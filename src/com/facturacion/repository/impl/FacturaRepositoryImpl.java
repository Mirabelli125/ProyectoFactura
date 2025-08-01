package com.facturacion.repository.impl;

import com.facturacion.model.Factura;
import com.facturacion.repository.FacturaRepository;
import com.facturacion.util.JsonUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementación de FacturaRepository que almacena los datos en archivos JSON.
 */
public class FacturaRepositoryImpl implements FacturaRepository {
    
    private static final String DATA_DIR = "data";
    private static final String FACTURAS_DIR = DATA_DIR + File.separator + "facturas";
    
    public FacturaRepositoryImpl() {
        // Crear directorios si no existen
        try {
            Files.createDirectories(Paths.get(FACTURAS_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Error al inicializar el repositorio de facturas", e);
        }
    }
    
    private Path getFacturaPath(int numeroFactura) {
        return Paths.get(FACTURAS_DIR, String.format("FACT_%06d.json", numeroFactura));
    }
    
    @Override
    public Factura guardar(Factura factura) {
        if (factura == null) {
            return null;
        }
        
        try {
            Path filePath = getFacturaPath(factura.getNumeroFactura());
            String json = JsonUtil.toJson(factura);
            Files.writeString(filePath, json);
            return factura;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la factura", e);
        }
    }
    
    @Override
    public Optional<Factura> buscarPorNumero(int numeroFactura) {
        Path filePath = getFacturaPath(numeroFactura);
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        
        try {
            String json = Files.readString(filePath);
            return Optional.of(JsonUtil.fromJson(json, Factura.class));
        } catch (IOException e) {
            throw new RuntimeException("Error al leer la factura con número: " + numeroFactura, e);
        }
    }
    
    @Override
    public List<Factura> buscarTodas() {
        try (Stream<Path> paths = Files.list(Paths.get(FACTURAS_DIR))) {
            return paths
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json"))
                .map(path -> {
                    try {
                        String json = Files.readString(path);
                        return JsonUtil.fromJson(json, Factura.class);
                    } catch (IOException e) {
                        throw new RuntimeException("Error al leer el archivo: " + path, e);
                    }
                })
                .sorted(Comparator.comparingInt(Factura::getNumeroFactura).reversed())
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error al listar las facturas", e);
        }
    }
    
    @Override
    public List<Factura> buscarPorCliente(long cedulaCliente) {
        return buscarTodas().stream()
            .filter(factura -> factura.getCliente().getCedula() == cedulaCliente)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Factura> buscarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }
        
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        
        return buscarTodas().stream()
            .filter(factura -> {
                LocalDate fechaFactura = factura.getFechaHora().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                return !fechaFactura.isBefore(fechaInicio) && !fechaFactura.isAfter(fechaFin);
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Factura> obtenerUltimaFactura() {
        return buscarTodas().stream()
            .max(Comparator.comparingInt(Factura::getNumeroFactura));
    }
    
    @Override
    public boolean existePorNumero(int numeroFactura) {
        return Files.exists(getFacturaPath(numeroFactura));
    }
}
