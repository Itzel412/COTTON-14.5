// En: com/franelas/cotton/perfil/PerfilService.java
package com.franelas.cotton.perfil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

@Service
public class PerfilService {

    // (¡Cambio!) Ruta al archivo de perfiles
    private final String RUTA_JSON = "backend/backend/src/main/resources/data/perfiles.json";

    /**
     * HISTORIA DE USUARIO: "Consultar Perfiles"
     */
    public List<Perfil> obtenerTodosLosPerfiles() {
        ObjectMapper mapper = new ObjectMapper();

        // (¡Cambio!) Usamos Perfil
        TypeReference<List<Perfil>> typeReference = new TypeReference<List<Perfil>>() {};

        try {
            File jsonFile = new File(RUTA_JSON);

            if (!jsonFile.exists() || jsonFile.length() == 0) {
                System.err.println("Error: No se pudo encontrar el archivo " + RUTA_JSON);
                return Collections.emptyList();
            }

            // (¡Cambio!) Leemos List<Perfil>
            List<Perfil> perfiles = mapper.readValue(jsonFile, typeReference);
            return perfiles;

        } catch (Exception e) {
            System.err.println("Error al leer el archivo JSON: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * HISTORIA DE USUARIO: "Crear Perfil"
     */
    public boolean registrarPerfil(Perfil nuevoPerfil) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            File jsonFile = new File(RUTA_JSON);
            List<Perfil> perfiles;

            if (jsonFile.exists() && jsonFile.length() > 0) {
                // (¡Cambio!) Usamos Perfil
                TypeReference<List<Perfil>> typeReference = new TypeReference<List<Perfil>>() {};
                perfiles = mapper.readValue(jsonFile, typeReference);
            } else {
                perfiles = new ArrayList<>();
                System.err.println("Archivo no encontrado o vacío, creando lista nueva: " + RUTA_JSON);
            }

            // (¡Cambio!) Asegurar un ID único para Perfil
            if (nuevoPerfil.getId() == 0) {
                long nextId = perfiles.stream().mapToLong(Perfil::getId).max().orElse(0) + 1;
                nuevoPerfil.setId(nextId);
            }

            perfiles.add(nuevoPerfil);
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, perfiles);

            System.out.println("Perfil " + nuevoPerfil.getNombre() + " registrado exitosamente");
            return true;  // éxito

        } catch (Exception e) {
            System.err.println("Error al escribir el archivo JSON: " + e.getMessage());
            e.printStackTrace();
            return false;  // fallo
        }
    }
}