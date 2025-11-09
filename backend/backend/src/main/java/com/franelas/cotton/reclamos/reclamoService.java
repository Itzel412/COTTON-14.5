package com.franelas.cotton.reclamos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class reclamoService {

    private final String RUTA_JSON = "backend/backend/src/main/resources/data/reclamos.json";
    private final ObjectMapper mapper = new ObjectMapper();

    public boolean registrarReclamo(reclamo nuevoReclamo) {
        try {
            File jsonFile = new File(RUTA_JSON);
            List<reclamo> reclamos;

            if (jsonFile.exists() && jsonFile.length() > 0) {
                reclamos = mapper.readValue(jsonFile, new TypeReference<List<reclamo>>() {});
            } else {
                reclamos = new ArrayList<>();
            }

            reclamos.add(nuevoReclamo);
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, reclamos);

            System.out.println("Reclamo guardado correctamente con ID: " + nuevoReclamo.getId());
            return true;
        } catch (Exception e) {
            System.err.println("Error al guardar reclamo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<reclamo> obtenerReclamos() {
        try {
            File jsonFile = new File(RUTA_JSON);
            if (!jsonFile.exists() || jsonFile.length() == 0) {
                return Collections.emptyList();
            }
            return mapper.readValue(jsonFile, new TypeReference<List<reclamo>>() {});
        } catch (Exception e) {
            System.err.println("Error al leer reclamos: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}