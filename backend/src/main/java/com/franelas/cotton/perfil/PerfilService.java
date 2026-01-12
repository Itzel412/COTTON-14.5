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

    public String registrarPerfil(Perfil nuevoPerfil) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            File jsonFile = new File(RUTA_JSON);
            List<Perfil> perfiles;

            if (jsonFile.exists() && jsonFile.length() > 0) {
                TypeReference<List<Perfil>> typeReference = new TypeReference<List<Perfil>>() {};
                perfiles = mapper.readValue(jsonFile, typeReference);
            } else {
                perfiles = new ArrayList<>();
                System.err.println("Archivo no encontrado o vacio, creando lista nueva: " + RUTA_JSON);
            }

            String correoNuevo = nuevoPerfil.getCorreo();


            String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
            if (correoNuevo == null || !correoNuevo.matches(emailRegex)) {

                return "Error: El correo '" + correoNuevo + "' no tiene un formato valido.";
            }

            boolean correoYaExiste = perfiles.stream()
                    .anyMatch(perfil -> perfil.getCorreo().equalsIgnoreCase(correoNuevo));

            if (correoYaExiste) {

                return "Error: El correo " + correoNuevo + " ya esta en uso.";
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

            return null;

        } catch (Exception e) {
            System.err.println("Error al escribir el archivo JSON: " + e.getMessage());
            e.printStackTrace();
            return "Error interno del servidor.";
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

    public String actualizarPerfil(Perfil perfilEditado) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            File jsonFile = new File(RUTA_JSON);
            List<Perfil> perfiles;

            if (jsonFile.exists() && jsonFile.length() > 0) {
                TypeReference<List<Perfil>> typeReference = new TypeReference<List<Perfil>>() {};
                perfiles = mapper.readValue(jsonFile, typeReference);
            } else {
                return "Error: No hay perfiles para editar.";
            }

            String correoNuevo = perfilEditado.getCorreo();
            String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
            if (correoNuevo == null || !correoNuevo.matches(emailRegex)) {
                return "Error: El correo '" + correoNuevo + "' no tiene un formato valido.";
            }

            boolean encontrado = false;
            for (Perfil p : perfiles) {
                if (p.getId() == perfilEditado.getId()) {
                    encontrado = true;
                } else if (p.getCorreo().equalsIgnoreCase(perfilEditado.getCorreo())) {
                    return "Error: El correo " + perfilEditado.getCorreo() + " ya esta en uso por otro perfil.";
                }
            }

            if (!encontrado) {
                return "Error: Perfil no encontrado para editar.";
            }

            for (int i = 0; i < perfiles.size(); i++) {
                Perfil p = perfiles.get(i);
                if (p.getId() == perfilEditado.getId()) {
                    p.setNombre(perfilEditado.getNombre());
                    p.setCorreo(perfilEditado.getCorreo());
                    p.setClave(perfilEditado.getClave());
                    p.setDireccion(perfilEditado.getDireccion());
                    p.setTelefono(perfilEditado.getTelefono());
                    p.setRol(perfilEditado.getRol());
                    perfiles.set(i, p);
                    break;
                }
            }

            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, perfiles);
            System.out.println("Perfil id=" + perfilEditado.getId() + " actualizado correctamente.");
            return null;

        } catch (Exception e) {
            System.err.println("Error al actualizar el archivo JSON: " + e.getMessage());
            e.printStackTrace();
            return "Error interno del servidor.";
        }
    }

    public String eliminarPerfil(long id) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            File jsonFile = new File(RUTA_JSON);
            List<Perfil> perfiles;

            if (jsonFile.exists() && jsonFile.length() > 0) {
                TypeReference<List<Perfil>> typeReference = new TypeReference<List<Perfil>>() {};
                perfiles = mapper.readValue(jsonFile, typeReference);
            } else {
                return "Error: No hay perfiles para eliminar.";
            }

            boolean eliminado = false;
            Iterator<Perfil> it = perfiles.iterator();
            while (it.hasNext()) {
                Perfil p = it.next();
                if (p.getId() == id) {
                    it.remove();
                    eliminado = true;
                    break;
                }
            }

            if (!eliminado) {
                return "Error: Perfil no encontrado para eliminar.";
            }

            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, perfiles);
            System.out.println("Perfil id=" + id + " eliminado correctamente.");
            return null;

        } catch (Exception e) {
            System.err.println("Error al eliminar del archivo JSON: " + e.getMessage());
            e.printStackTrace();
            return "Error interno del servidor.";
        }
    }
}