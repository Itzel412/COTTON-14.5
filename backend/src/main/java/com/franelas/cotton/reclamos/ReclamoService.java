package com.franelas.cotton.reclamos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ReclamoService {

    private final String RUTA_JSON = "backend/backend/src/main/resources/data/reclamos.json";
    private final ObjectMapper mapper = new ObjectMapper();

    public boolean registrarReclamo(Reclamo nuevoReclamo) {
        try {
            File jsonFile = new File(RUTA_JSON);
            List<Reclamo> Reclamos;

            if (jsonFile.exists() && jsonFile.length() > 0) {
                Reclamos = mapper.readValue(jsonFile, new TypeReference<List<Reclamo>>() {});
            } else {
                Reclamos = new ArrayList<>();
            }

            Reclamos.add(nuevoReclamo);
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, Reclamos);

            System.out.println("Reclamo guardado correctamente con ID: " + nuevoReclamo.getId());
            return true;
        } catch (Exception e) {
            System.err.println("Error al guardar reclamo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Reclamo> obtenerReclamos() {
        try {
            File jsonFile = new File(RUTA_JSON);
            if (!jsonFile.exists() || jsonFile.length() == 0) {
                return Collections.emptyList();
            }
            return mapper.readValue(jsonFile, new TypeReference<List<Reclamo>>() {});
        } catch (Exception e) {
            System.err.println("Error al leer reclamos: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}