package com.franelas.cotton.inventario;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ProductoService {

    private final String RUTA_JSON = "backend/backend/src/main/resources/data/productos.json";

    private final ObjectMapper mapper = new ObjectMapper();

    public List<Producto> obtenerTodosLosProductos() {

        TypeReference<List<Producto>> typeReference = new TypeReference<List<Producto>>() {};

        try {
            File jsonFile = new File(RUTA_JSON);

            if (!jsonFile.exists() || jsonFile.length() == 0) {
                System.err.println("Error: No se pudo encontrar el archivo " + RUTA_JSON);
                return Collections.emptyList();
            }

            return mapper.readValue(jsonFile, typeReference);

        } catch (Exception e) {
            System.err.println("Error al leer el archivo JSON de productos: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public boolean registrarProducto(Producto nuevoProducto) {

        try {
            File jsonFile = new File(RUTA_JSON);
            List<Producto> productos;

            if (jsonFile.exists() && jsonFile.length() > 0) {
                TypeReference<List<Producto>> typeReference = new TypeReference<List<Producto>>() {};
                productos = mapper.readValue(jsonFile, typeReference);
            } else {
                productos = new ArrayList<>();
                System.err.println("Archivo de productos no encontrado o vacío, creando lista nueva: " + RUTA_JSON);
            }

            if (nuevoProducto.getId() == 0) {
                long nextId = productos.stream()
                        .mapToLong(Producto::getId)
                        .max()
                        .orElse(0) + 1;
                nuevoProducto.setId(nextId);
            }

            productos.add(nuevoProducto);
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, productos);

            System.out.println("Producto " + nuevoProducto.getNombre() + " registrado exitosamente");
            return true;

        } catch (Exception e) {
            System.err.println("Error al registrar producto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean guardarProductos(List<Producto> productosActualizados) {
        try {
            File jsonFile = new File(RUTA_JSON);
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, productosActualizados);
            System.out.println("Inventario actualizado correctamente.");
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar el archivo de productos: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
