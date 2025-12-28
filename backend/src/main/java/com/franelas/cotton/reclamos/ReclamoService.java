package com.franelas.cotton.reclamos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class ReclamoService {

    private final String RUTA_JSON = "src/main/resources/data/reclamos.json";

    private final ObjectMapper mapper = new ObjectMapper();

    private static final List<String> ESTADOS_VALIDOS = Arrays.asList(
            "PENDIENTE", "EN_PROCESO", "CERRADO"
    );

    private List<Reclamo> leerReclamos() {
        try {
            File jsonFile = new File(RUTA_JSON);
            if (!jsonFile.exists() || jsonFile.length() == 0) {
                return new ArrayList<>();
            }
            return mapper.readValue(jsonFile, new TypeReference<List<Reclamo>>() {});
        } catch (Exception e) {
            System.err.println("Error al leer reclamos: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private boolean guardarReclamos(List<Reclamo> reclamos) {
        try {
            File jsonFile = new File(RUTA_JSON);
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, reclamos);
            return true;
        } catch (Exception e) {
            System.err.println("Error al guardar reclamos: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Reclamo> obtenerTodos() {
        List<Reclamo> lista = leerReclamos();
        return Collections.unmodifiableList(lista);
    }

    public boolean crearReclamo(Reclamo nuevo) {
        try {
            if (nuevo == null) {
                return false;
            }
            if (nuevo.getDescripcion() == null || nuevo.getDescripcion().trim().length() < 50) {
                System.err.println("Descripción muy corta.");
                return false;
            }

            nuevo.setEstado("PENDIENTE");

            List<Reclamo> reclamos = leerReclamos();

            long nextId = reclamos.stream()
                    .mapToLong(Reclamo::getId)
                    .max()
                    .orElse(0L) + 1L;
            nuevo.setId(nextId);

            reclamos.add(nuevo);
            return guardarReclamos(reclamos);

        } catch (Exception e) {
            System.err.println("Error al crear reclamo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarEstado(long id, String nuevoEstado) {
        try {
            if (nuevoEstado == null) {
                System.err.println("Estado nulo");
                return false;
            }

            String estadoNormalizado = nuevoEstado.trim().toUpperCase();
            if (!ESTADOS_VALIDOS.contains(estadoNormalizado)) {
                System.err.println("Estado inválido: " + nuevoEstado);
                return false;
            }

            List<Reclamo> reclamos = leerReclamos();
            if (reclamos.isEmpty()) {
                System.err.println("No hay reclamos para actualizar");
                return false;
            }

            Reclamo encontrado = reclamos.stream()
                    .filter(r -> r.getId() == id)
                    .findFirst()
                    .orElse(null);

            if (encontrado == null) {
                System.err.println("No se encontró reclamo con id " + id);
                return false;
            }

            encontrado.setEstado(estadoNormalizado);
            return guardarReclamos(reclamos);

        } catch (Exception e) {
            System.err.println("Error actualizando estado de reclamo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
