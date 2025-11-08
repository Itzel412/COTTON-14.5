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

    // (1. ¡Cambio!) Usamos la ruta física para poder leer Y escribir.
    private final String RUTA_JSON = "src/main/resources/data/perfiles.json";

    /**
     * HISTORIA DE USUARIO: "Consultar Perfiles"
     * Lee el archivo perfiles.json y lo devuelve como una lista de Perfil.
     */
    public List<Perfil> obtenerTodosLosPerfiles() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File jsonFile = new File(RUTA_JSON);

            // (2. Lógica de lectura adaptada para usar 'File')
            if (!jsonFile.exists() || jsonFile.length() == 0) {
                System.err.println("Error: No se pudo encontrar el archivo " + RUTA_JSON);
                return Collections.emptyList();
            }

            // (3. ¡Cambio!) Se usa Perfil en lugar de Producto
            TypeReference<List<Perfil>> typeReference = new TypeReference<List<Perfil>>() {};
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
     * Le entran los datos de un perfil, busca el archivo json o lo crea y agrega el perfil al archivo
     *
     * @param nuevoPerfil El perfil a añadir.
     * @return true si el perfil se guardó, false si no.
     */
    public boolean registrarPerfil(Perfil nuevoPerfil) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            File jsonFile = new File(RUTA_JSON);
            List<Perfil> perfiles;

            if (jsonFile.exists() && jsonFile.length() > 0) {
                // (4. ¡Cambio!) Se usa Perfil
                TypeReference<List<Perfil>> typeReference = new TypeReference<List<Perfil>>() {};
                perfiles = mapper.readValue(jsonFile, typeReference);
            } else {
                perfiles = new ArrayList<>();
                System.err.println("Archivo no encontrado o vacío, creando lista nueva: " + RUTA_JSON);
            }

            // (5. ¡Cambio!) Asegurar un ID único para Perfil
            if (nuevoPerfil.getId() == 0) {
                long nextId = perfiles.stream().mapToLong(Perfil::getId).max().orElse(0) + 1;
                nuevoPerfil.setId(nextId);
            }

            // Agregar perfil a la lista
            perfiles.add(nuevoPerfil);

            // Escribir la lista actualizada de vuelta al JSON
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