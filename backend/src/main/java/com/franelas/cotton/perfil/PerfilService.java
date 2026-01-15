package com.franelas.cotton.perfil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PerfilService {

    private final String RUTA_JSON = "src/main/resources/data/perfiles.json";
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private String normEmail(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private String normRol(String s) {
        return s == null ? "" : s.trim().toUpperCase();
    }

    public List<Perfil> obtenerTodosLosPerfiles() {
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

    public Perfil buscarPorId(long id) {
        return obtenerTodosLosPerfiles().stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public Perfil buscarPorCorreo(String correo) {
        String c = normEmail(correo);
        if (c.isEmpty()) return null;

        return obtenerTodosLosPerfiles().stream()
                .filter(p -> normEmail(p.getCorreo()).equals(c))
                .findFirst()
                .orElse(null);
    }

    public String registrarPerfil(Perfil nuevoPerfil) {
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

            if (nuevoPerfil == null) return "Error: Perfil inválido.";
            if (nuevoPerfil.getNombre() == null || nuevoPerfil.getNombre().trim().isEmpty()) return "Error: Nombre obligatorio.";
            if (nuevoPerfil.getDireccion() == null || nuevoPerfil.getDireccion().trim().isEmpty()) return "Error: Dirección obligatoria.";
            if (nuevoPerfil.getTelefono() == null || nuevoPerfil.getTelefono().trim().isEmpty()) return "Error: Teléfono obligatorio.";

            String correoNuevo = nuevoPerfil.getCorreo();
            if (correoNuevo == null || !correoNuevo.matches(EMAIL_REGEX)) {
                return "Error: El correo '" + correoNuevo + "' no tiene un formato valido.";
            }

            if (nuevoPerfil.getClave() == null || nuevoPerfil.getClave().trim().isEmpty()) {
                return "Error: Clave obligatoria.";
            }

            // Rol: solo ADMIN o CLIENTE. Default CLIENTE.
            String rol = normRol(nuevoPerfil.getRol());
            if (rol.isEmpty()) rol = "CLIENTE";
            if (!"ADMIN".equals(rol) && !"CLIENTE".equals(rol)) {
                return "Error: Rol inválido.";
            }
            nuevoPerfil.setRol(rol);

            boolean correoYaExiste = perfiles.stream()
                    .anyMatch(perfil -> normEmail(perfil.getCorreo()).equals(normEmail(correoNuevo)));

            if (correoYaExiste) {
                return "Error: El correo " + correoNuevo + " ya esta en uso.";
            }

            long nextId = perfiles.stream()
                    .mapToLong(Perfil::getId)
                    .max()
                    .orElse(0) + 1;
            nuevoPerfil.setId(nextId);

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
        if (correo == null || clave == null) return null;

        return obtenerTodosLosPerfiles().stream()
                .filter(p -> normEmail(p.getCorreo()).equals(normEmail(correo))
                        && (p.getClave() != null && p.getClave().equals(clave)))
                .findFirst()
                .orElse(null);
    }

    public String actualizarPerfil(Perfil original, Perfil perfilEditado) {
        try {
            File jsonFile = new File(RUTA_JSON);
            List<Perfil> perfiles;

            if (jsonFile.exists() && jsonFile.length() > 0) {
                TypeReference<List<Perfil>> typeReference = new TypeReference<List<Perfil>>() {};
                perfiles = mapper.readValue(jsonFile, typeReference);
            } else {
                return "Error: No hay perfiles para editar.";
            }

            if (perfilEditado == null) return "Error: Perfil inválido.";

            String correoNuevo = perfilEditado.getCorreo();
            if (correoNuevo == null || !correoNuevo.matches(EMAIL_REGEX)) {
                return "Error: El correo '" + correoNuevo + "' no tiene un formato valido.";
            }

            String rol = normRol(perfilEditado.getRol());
            if (rol.isEmpty()) rol = normRol(original.getRol());
            if (!"ADMIN".equals(rol) && !"CLIENTE".equals(rol)) {
                return "Error: Rol inválido.";
            }
            perfilEditado.setRol(rol);

            boolean existeId = false;
            for (Perfil p : perfiles) {
                if (p.getId() == perfilEditado.getId()) {
                    existeId = true;
                } else if (normEmail(p.getCorreo()).equals(normEmail(perfilEditado.getCorreo()))) {
                    return "Error: El correo " + perfilEditado.getCorreo() + " ya esta en uso por otro perfil.";
                }
            }

            if (!existeId) {
                return "Error: Perfil no encontrado para editar.";
            }

            for (int i = 0; i < perfiles.size(); i++) {
                Perfil p = perfiles.get(i);
                if (p.getId() == perfilEditado.getId()) {
                    p.setNombre(perfilEditado.getNombre());
                    p.setCorreo(perfilEditado.getCorreo());
                    p.setDireccion(perfilEditado.getDireccion());
                    p.setTelefono(perfilEditado.getTelefono());
                    p.setRol(perfilEditado.getRol());

                    if (perfilEditado.getClave() != null && !perfilEditado.getClave().trim().isEmpty()) {
                        p.setClave(perfilEditado.getClave());
                    }

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
        try {
            File jsonFile = new File(RUTA_JSON);
            List<Perfil> perfiles;

            if (jsonFile.exists() && jsonFile.length() > 0) {
                TypeReference<List<Perfil>> typeReference = new TypeReference<List<Perfil>>() {};
                perfiles = mapper.readValue(jsonFile, typeReference);
            } else {
                return "Error: No hay perfiles para eliminar.";
            }

            boolean eliminado = perfiles.removeIf(p -> p.getId() == id);

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
