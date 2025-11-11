package com.franelas.cotton.inventario;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class ProductoService {

    private final String RUTA_JSON = "src/main/resources/data/productos.json";


    private final ObjectMapper mapper = new ObjectMapper();

    public List<Producto> obtenerTodosLosProductos() {
        try {
            File jsonFile = new File(RUTA_JSON);

            if (!jsonFile.exists() || jsonFile.length() == 0) {
                System.err.println("Error: No se pudo encontrar el archivo " + RUTA_JSON);
                return Collections.emptyList();
            }

            return mapper.readValue(jsonFile, new TypeReference<List<Producto>>() {});
        } catch (Exception e) {
            System.err.println("Error al leer el archivo JSON de productos: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private void validarProducto(Producto p) {
        if (p == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }

        if (p.getPrecio() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }

        if (p.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }

        List<String> tallasValidas = Arrays.asList("S", "M", "L", "XL");
        if (p.getTalla() == null ||
                !tallasValidas.contains(p.getTalla().toUpperCase())) {
            throw new IllegalArgumentException("La talla debe ser S, M, L o XL");
        }

        List<String> coloresValidos = Arrays.asList(
                "Blanco", "Negro", "Rojo", "Azul", "Amarillo", "Verde", "Morado"
        );
        boolean colorOk = false;
        if (p.getColor() != null) {
            for (String c : coloresValidos) {
                if (c.equalsIgnoreCase(p.getColor())) {
                    colorOk = true;
                    p.setColor(c);
                    break;
                }
            }
        }
        if (!colorOk) {
            throw new IllegalArgumentException("El color del producto no es válido");
        }

        p.setTalla(p.getTalla().toUpperCase());
    }

    public boolean registrarProducto(Producto nuevoProducto) {
        try {
            validarProducto(nuevoProducto);

            File jsonFile = new File(RUTA_JSON);
            List<Producto> productos;

            if (jsonFile.exists() && jsonFile.length() > 0) {
                productos = mapper.readValue(jsonFile, new TypeReference<List<Producto>>() {});
            } else {
                productos = new ArrayList<>();
                System.err.println("Archivo no encontrado o vacío, creando lista nueva: " + RUTA_JSON);
            }

            productos.add(nuevoProducto);
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, productos);

            System.out.println("Producto registrado exitosamente");
            return true;

        } catch (IllegalArgumentException e) {
            System.err.println("Validación de producto falló: " + e.getMessage());
            return false;

        } catch (Exception e) {
            System.err.println("Error al registrar el producto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean guardarProductos(List<Producto> productos) {
        try {
            File jsonFile = new File(RUTA_JSON);
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, productos);
            System.out.println("Inventario actualizado correctamente.");
            return true;
        } catch (Exception e) {
            System.err.println("Error al guardar productos en inventario: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
