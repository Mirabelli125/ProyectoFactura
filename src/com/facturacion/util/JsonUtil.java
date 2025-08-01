package com.facturacion.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;

/**
 * Utilidad para la serialización y deserialización de objetos a/desde JSON.
 * Utiliza Jackson para el procesamiento de JSON.
 */
public class JsonUtil {
    
    private static final ObjectMapper objectMapper = createObjectMapper();
    
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Configurar el ObjectMapper
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Registrar módulo personalizado si es necesario
        SimpleModule module = new SimpleModule();
        // Aquí se pueden agregar serializadores/deserializadores personalizados
        mapper.registerModule(module);
        
        return mapper;
    }
    
    /**
     * Convierte un objeto a su representación JSON.
     * 
     * @param obj Objeto a serializar
     * @return Cadena JSON
     * @throws RuntimeException Si ocurre un error durante la serialización
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al convertir objeto a JSON", e);
        }
    }
    
    /**
     * Convierte una cadena JSON a un objeto del tipo especificado.
     * 
     * @param <T> Tipo del objeto resultante
     * @param json Cadena JSON
     * @param clazz Clase del objeto resultante
     * @return Objeto deserializado
     * @throws RuntimeException Si ocurre un error durante la deserialización
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al convertir JSON a objeto", e);
        }
    }
    
    /**
     * Convierte un objeto a una cadena JSON con formato legible.
     * 
     * @param obj Objeto a formatear
     * @return Cadena JSON con formato
     * @throws RuntimeException Si ocurre un error durante la serialización
     */
    public static String toPrettyJson(Object obj) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al formatear objeto a JSON", e);
        }
    }
}
