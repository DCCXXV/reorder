package com.thecritics.reorder.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter para transformar una {@code List<List<String>>} a una columna de base de datos String (JSON)
 * y viceversa.  Utiliza Jackson ObjectMapper para realizar la serialización y deserialización.
 */
@Converter
public class ListOfListsConverter implements AttributeConverter<List<List<String>>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Convierte la lista de listas de strings en una representación JSON para almacenarla en la base de datos.
     *
     * @param attribute La lista de listas de strings a convertir.
     * @return Una cadena JSON que representa la lista, o null si la lista es null.
     * @throws IllegalArgumentException si hay un error al convertir la lista a JSON.
     */
    @Override
    public String convertToDatabaseColumn(List<List<String>> attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error al convertir lista a JSON", e);
        }
    }

    /**
     * Convierte una cadena JSON de la base de datos en una lista de listas de strings.
     *
     * @param dbData La cadena JSON recuperada de la base de datos.
     * @return La lista de listas de strings representada por la cadena JSON,
     *         o una nueva lista vacía si la cadena es null o vacía.
     * @throws IllegalArgumentException si hay un error al convertir el JSON a lista.
     */
    @Override
    public List<List<String>> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<List<String>>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error al convertir JSON a lista", e);
        }
    }
}
