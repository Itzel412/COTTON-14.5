package com.franelas.cotton.facturas;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class facturaService {

    private final String RUTA_JSON = "backend/backend/src/main/resources/data/facturas.json";
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Registra una nueva factura en el archivo JSON.
     */
    public boolean registrarFactura(factura nuevaFactura) {
        try {
            File jsonFile = new File(RUTA_JSON);
            List<factura> facturas;

            if (jsonFile.exists() && jsonFile.length() > 0) {
                facturas = mapper.readValue(jsonFile, new TypeReference<List<factura>>() {});
            } else {
                facturas = new ArrayList<>();
            }

            // Si no trae id, se asume que el constructor ya la asignó
            if (nuevaFactura.getId() == null || nuevaFactura.getId().isEmpty()) {
                // no se fuerza cambio: constructor debe asignar id
            }

            facturas.add(nuevaFactura);
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, facturas);

            System.out.println("Factura guardada correctamente con ID: " + nuevaFactura.getId());
            return true;
        } catch (Exception e) {
            System.err.println("Error al guardar factura: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene todas las facturas desde el archivo JSON.
     */
    public List<factura> obtenerFacturas() {
        try {
            File jsonFile = new File(RUTA_JSON);
            if (!jsonFile.exists() || jsonFile.length() == 0) {
                return Collections.emptyList();
            }
            return mapper.readValue(jsonFile, new TypeReference<List<factura>>() {});
        } catch (Exception e) {
            System.err.println("Error al leer facturas: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}

