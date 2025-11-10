package com.franelas.cotton.inventario;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
// import java.io.InputStream; // <-- Ya no lo necesitamos
import java.util.ArrayList; // <-- Importado para la lista
import java.util.Collections;
import java.util.List;

@Service
public class ProductoService {

    // Esta ruta es correcta para new File() desde la raíz del proyecto
    private final String RUTA_JSON = "backend/backend/src/main/resources/data/productos.json";

    /**
     * HISTORIA DE USUARIO: "Consultar Productos"
     * (¡AHORA CORREGIDO!) Usa new File() para ser consistente con el método de escritura.
     */
    public List<Producto> obtenerTodosLosProductos() {

        ObjectMapper mapper = new ObjectMapper();
        TypeReference<List<Producto>> typeReference = new TypeReference<List<Producto>>() {};

        try {
            // (¡CAMBIO!) Usamos File, igual que en el método de registrar
            File jsonFile = new File(RUTA_JSON);

            if (!jsonFile.exists() || jsonFile.length() == 0) {
                System.err.println("Error: No se pudo encontrar el archivo " + RUTA_JSON);
                return Collections.emptyList();
            }

            // (¡CAMBIO!) Leemos directamente del archivo
            List<Producto> productos = mapper.readValue(jsonFile, typeReference);
            return productos;

        } catch (Exception e) {
            System.err.println("Error al leer el archivo JSON: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * HISTORIA DE USUARIO: "Registrar Producto"
     * (Esta lógica ya era correcta)
     */
    public boolean registrarProducto(Producto nuevoProducto) {

        ObjectMapper mapper = new ObjectMapper();

        try {
            File jsonFile = new File(RUTA_JSON);
            List<Producto> productos;

            if (jsonFile.exists() && jsonFile.length() > 0) {
                TypeReference<List<Producto>> typeReference = new TypeReference<List<Producto>>() {};
                productos = mapper.readValue(jsonFile, typeReference);
            } else {
                productos = new ArrayList<>(); // Usamos ArrayList
                System.err.println("Error al leer el archivo " + RUTA_JSON);
            }

            if (nuevoProducto.getId() == 0) {
                long nextId = productos.stream().mapToLong(Producto::getId).max().orElse(0) + 1;
                nuevoProducto.setId(nextId);
            }

            productos.add(nuevoProducto);
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, productos);

            System.out.println("Producto" + nuevoProducto.getNombre() + " registrado exitosamente");
            return true;

        } catch (Exception e) {
            System.err.println("Error al leer el archivo " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}