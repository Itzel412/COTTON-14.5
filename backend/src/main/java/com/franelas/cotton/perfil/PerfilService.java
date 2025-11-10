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

    private final String RUTA_JSON = "src/main/resources/data/perfiles.json";

    public List<Perfil> obtenerTodosLosPerfiles() {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<List<Perfil>> typeReference = new TypeReference<List<Perfil>>() {};

        try {
            File jsonFile = new File(RUTA_JSON);

            if (!jsonFile.exists() || jsonFile.length() == 0) {
                System.err.println("Error: No se pudo encontrar el archivo " + RUTA_JSON);
                return Collections.emptyList();
            }

            return mapper.readValue(jsonFile, typeReference);

        } catch (Exception e) {
            System.err.println("Error al leer el archivo JSON: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public boolean registrarPerfil(Perfil nuevoPerfil) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            File jsonFile = new File(RUTA_JSON);
            List<Perfil> perfiles;

            if (jsonFile.exists() && jsonFile.length() > 0) {
                TypeReference<List<Perfil>> typeReference = new TypeReference<List<Perfil>>() {};
                perfiles = mapper.readValue(jsonFile, typeReference);
            } else {
                perfiles = new ArrayList<>();
                System.err.println("Archivo no encontrado o vacío, creando lista nueva: " + RUTA_JSON);
            }

            if (nuevoPerfil.getId() == 0) {
                long nextId = perfiles.stream()
                        .mapToLong(Perfil::getId)
                        .max()
                        .orElse(0) + 1;
                nuevoPerfil.setId(nextId);
            }

            perfiles.add(nuevoPerfil);
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, perfiles);

            System.out.println("Perfil " + nuevoPerfil.getNombre() + " registrado exitosamente");
            return true;

        } catch (Exception e) {
            System.err.println("Error al escribir el archivo JSON: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Perfil buscarPorCorreoYClave(String correo, String clave) {
        List<Perfil> perfiles = obtenerTodosLosPerfiles();

        return perfiles.stream()
                .filter(p -> p.getCorreo().equalsIgnoreCase(correo)
                        && p.getClave().equals(clave))
                .findFirst()
                .orElse(null);
    }
}
