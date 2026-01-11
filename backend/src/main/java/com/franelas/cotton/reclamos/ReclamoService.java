package com.franelas.cotton.reclamos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReclamoService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Path reclamosJsonPath;

    private static final List<String> ESTADOS_VALIDOS = Arrays.asList(
            "PENDIENTE", "EN_PROCESO", "CERRADO"
    );

    public ReclamoService() {
        this.reclamosJsonPath = resolveReclamosJsonPath();
        validarArchivoExiste(this.reclamosJsonPath);

        System.out.println("ReclamoService usando reclamos.json en: " + this.reclamosJsonPath.toAbsolutePath());
    }

    private Path resolveReclamosJsonPath() {
        Path base = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();

        Path[] candidates = new Path[] {
                base.resolve("src/main/resources/data/reclamos.json"),
                base.resolve("backend/src/main/resources/data/reclamos.json"),
                base.resolve("..").resolve("backend/src/main/resources/data/reclamos.json").normalize()
        };

        for (Path p : candidates) {
            if (Files.exists(p)) return p;
        }

        return candidates[0];
    }

    private void validarArchivoExiste(Path filePath) {
        if (filePath == null) {
            throw new IllegalStateException("Ruta de reclamos.json es null.");
        }
        if (!Files.exists(filePath)) {
            throw new IllegalStateException(
                    "No existe reclamos.json en la ruta esperada: " + filePath.toAbsolutePath() +
                            " (No se creará automáticamente; debes colocarlo tú)."
            );
        }
        if (!Files.isRegularFile(filePath)) {
            throw new IllegalStateException("La ruta no es un archivo válido: " + filePath.toAbsolutePath());
        }
    }

    private File getJsonFile() {
        return reclamosJsonPath.toFile();
    }

    private List<Reclamo> leerReclamos() {
        try {
            File jsonFile = getJsonFile();
            if (!jsonFile.exists() || jsonFile.length() == 0) return new ArrayList<>();
            return mapper.readValue(jsonFile, new TypeReference<List<Reclamo>>() {});
        } catch (Exception e) {
            System.err.println("Error al leer reclamos.json: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private boolean guardarReclamos(List<Reclamo> reclamos) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(getJsonFile(), reclamos);
            return true;
        } catch (Exception e) {
            System.err.println("Error al guardar reclamos.json: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String normEmail(String s) {
        return (s == null) ? "" : s.trim().toLowerCase();
    }

    public List<Reclamo> obtenerTodos() {
        List<Reclamo> lista = leerReclamos();
        lista.sort(Comparator.comparingLong(Reclamo::getId).reversed());
        return Collections.unmodifiableList(lista);
    }

    public List<Reclamo> obtenerPorUsuario(String email) {
        String emailN = normEmail(email);
        if (emailN.isEmpty()) return Collections.emptyList();

        List<Reclamo> lista = leerReclamos().stream()
                .filter(r -> normEmail(r.getUsuario()).equals(emailN))
                .sorted(Comparator.comparingLong(Reclamo::getId).reversed())
                .collect(Collectors.toList());

        return Collections.unmodifiableList(lista);
    }

    public boolean crearReclamo(Reclamo nuevo) {
        try {
            if (nuevo == null) return false;

            if (nuevo.getUsuario() == null || nuevo.getUsuario().trim().isEmpty()) {
                System.err.println("Usuario obligatorio.");
                return false;
            }
            if (nuevo.getTitulo() == null || nuevo.getTitulo().trim().isEmpty()) {
                System.err.println("Título obligatorio.");
                return false;
            }
            if (nuevo.getDescripcion() == null || nuevo.getDescripcion().trim().length() < 50) {
                System.err.println("Descripción muy corta.");
                return false;
            }

            nuevo.setEstado("PENDIENTE");

            if (nuevo.getFechaCreacion() == null || nuevo.getFechaCreacion().trim().isEmpty()) {
                nuevo.setFechaCreacion(LocalDate.now().toString()); // YYYY-MM-DD
            }

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
            if (nuevoEstado == null) return false;

            String estadoNormalizado = nuevoEstado.trim().toUpperCase();
            if (!ESTADOS_VALIDOS.contains(estadoNormalizado)) {
                System.err.println("Estado inválido: " + nuevoEstado);
                return false;
            }

            List<Reclamo> reclamos = leerReclamos();
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

    public boolean eliminarReclamo(long id) {
        try {
            List<Reclamo> reclamos = leerReclamos();
            boolean eliminado = reclamos.removeIf(r -> r.getId() == id);
            if (!eliminado) return false;
            return guardarReclamos(reclamos);
        } catch (Exception e) {
            System.err.println("Error eliminando reclamo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
