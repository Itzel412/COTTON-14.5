package com.franelas.cotton.inventario;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

            List<Producto> productos = mapper.readValue(jsonFile, new TypeReference<List<Producto>>() {});

            boolean changed = normalizarIdsSiHaceFalta(productos);
            if (changed) {
                guardarProductos(productos);
            }

            return productos;

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
        if (p.getTalla() == null || !tallasValidas.contains(p.getTalla().toUpperCase())) {
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

    private long obtenerMaxId(List<Producto> productos) {
        long max = 0;
        for (Producto p : productos) {
            if (p != null && p.getId() > max) {
                max = p.getId();
            }
        }
        return max;
    }

    private boolean normalizarIdsSiHaceFalta(List<Producto> productos) {
        if (productos == null || productos.isEmpty()) {
            return false;
        }

        boolean changed = false;
        Set<Long> usados = new HashSet<>();

        long maxId = obtenerMaxId(productos);

        for (Producto p : productos) {
            if (p == null) continue;

            long id = p.getId();
            boolean invalido = id <= 0;
            boolean duplicado = !invalido && usados.contains(id);

            if (invalido || duplicado) {
                maxId++;
                p.setId(maxId);
                changed = true;
            }

            usados.add(p.getId());
        }

        return changed;
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

            if (nuevoProducto.getId() <= 0) {
                long maxId = 0;
                for (Producto p : productos) {
                    if (p.getId() > maxId) maxId = p.getId();
                }
                nuevoProducto.setId(maxId + 1);
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

    public boolean eliminarProducto(long id) {
        try {
            if (id <= 0) {
                System.err.println("Error: El ID es inválido para eliminar: " + id);
                return false;
            }

            File jsonFile = new File(RUTA_JSON);

            if (!jsonFile.exists() || jsonFile.length() == 0) {
                System.err.println("Error: No hay productos registrados para eliminar.");
                return false;
            }

            List<Producto> productos = mapper.readValue(jsonFile, new TypeReference<List<Producto>>() {});

            boolean eliminado = false;

            for (int i = 0; i < productos.size(); i++) {
                if (productos.get(i).getId() == id) {
                    productos.remove(i);
                    eliminado = true;
                    break;
                }
            }

            if (!eliminado) {
                System.err.println("No se encontró un producto con el ID: " + id);
                return false;
            }

            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, productos);
            System.out.println("Producto eliminado correctamente.");
            return true;

        } catch (Exception e) {
            System.err.println("Error al eliminar el producto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean editarProducto(Producto productoActualizado) {
        try {
            if (productoActualizado == null || productoActualizado.getId() <= 0) {
                System.err.println("Error: El ID es obligatorio para editar.");
                return false;
            }

            validarProducto(productoActualizado);

            File jsonFile = new File(RUTA_JSON);

            if (!jsonFile.exists() || jsonFile.length() == 0) {
                System.err.println("Error: No hay productos registrados para editar.");
                return false;
            }

            List<Producto> productos = mapper.readValue(jsonFile, new TypeReference<List<Producto>>() {});

            boolean encontrado = false;

            for (int i = 0; i < productos.size(); i++) {
                Producto p = productos.get(i);
                if (p.getId() == productoActualizado.getId()) {
                    productos.set(i, productoActualizado);
                    encontrado = true;
                    break;
                }
            }

            if (!encontrado) {
                System.err.println("No se encontró un producto con el ID: " + productoActualizado.getId());
                return false;
            }

            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, productos);
            System.out.println("Producto editado correctamente.");
            return true;

        } catch (IllegalArgumentException e) {
            System.err.println("Validación del producto falló: " + e.getMessage());
            return false;

        } catch (Exception e) {
            System.err.println("Error al editar el producto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
