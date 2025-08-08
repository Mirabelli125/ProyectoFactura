package com.facturacion.repository.impl;

import com.facturacion.model.Factura;
import com.facturacion.repository.FacturaRepository;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
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
        return Paths.get(FACTURAS_DIR, String.format("FACT_%06d.dat", numeroFactura));
    }
    
    @Override
    public Factura guardar(Factura factura) {
        if (factura == null) {
            return null;
        }
        
        try (FileOutputStream fileOut = new FileOutputStream(getFacturaPath(factura.getNumeroFactura()).toFile());
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(factura);
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
        
        try (FileInputStream fileIn = new FileInputStream(filePath.toFile());
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            Factura factura = (Factura) in.readObject();
            return Optional.of(factura);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error al leer la factura con número: " + numeroFactura, e);
        }
    }
    
    @Override
    public List<Factura> buscarTodas() {
        try (Stream<Path> paths = Files.list(Paths.get(FACTURAS_DIR))) {
            return paths
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".dat"))
                .map(path -> {
                    try (FileInputStream fileIn = new FileInputStream(path.toFile());
                         ObjectInputStream in = new ObjectInputStream(fileIn)) {
                        return (Factura) in.readObject();
                    } catch (IOException | ClassNotFoundException e) {
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
            .filter(factura -> factura != null && 
                             factura.getCliente() != null && 
                             factura.getCliente().getCedula() == cedulaCliente)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Factura> buscarDelDia() {
        Calendar cal = Calendar.getInstance();
        Date hoy = cal.getTime();
        return buscarPorRangoFechas(hoy, hoy);
    }
    
    @Override
    public List<Factura> buscarPorRangoFechas(Date fechaInicio, Date fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }
        
        // Normalizar fechas para comparación (sin hora/minuto/segundo)
        Calendar calInicio = Calendar.getInstance();
        calInicio.setTime(fechaInicio);
        setToStartOfDay(calInicio);
        
        Calendar calFin = Calendar.getInstance();
        calFin.setTime(fechaFin);
        setToEndOfDay(calFin);
        
        final Date startDate = calInicio.getTime();
        final Date endDate = calFin.getTime();
        
        return buscarTodas().stream()
            .filter(factura -> factura != null && factura.getFechaFactura() != null)
            .filter(factura -> {
                Date facturaDate = factura.getFechaFactura();
                return (facturaDate.equals(startDate) || facturaDate.after(startDate)) &&
                       (facturaDate.before(endDate) || facturaDate.equals(endDate));
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Establece la hora, minuto, segundo y milisegundo al inicio del día (00:00:00.000).
     */
    private void setToStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
    
    /**
     * Establece la hora, minuto, segundo y milisegundo al final del día (23:59:59.999).
     */
    private void setToEndOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
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
