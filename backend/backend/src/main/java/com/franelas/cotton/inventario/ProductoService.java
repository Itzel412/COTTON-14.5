// itzel puso esto para tomarlo de ejemplo


package com.franelas.cotton.inventario;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service; // (Importante para Spring)

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Service // <-- (1. Le dice a Spring que esta clase es un Servicio)
public class ProductoService {

    private final String RUTA_JSON = "data/productos.json"; // (2. La ruta en 'resources')

    /**
     * Esta es la Historia de Usuario: "Consultar Productos"
     * Lee el archivo productos.json y lo devuelve como una lista de objetos Producto.
     *
     * @return Lista de todos los productos, o una lista vacía si hay un error.
     */
    public List<Producto> obtenerTodosLosProductos() {

        // (3. El "cerebro" de Jackson para leer JSON)
        ObjectMapper mapper = new ObjectMapper();

        // (4. Esto es necesario para decirle a Jackson que es una *Lista* de Productos)
        TypeReference<List<Producto>> typeReference = new TypeReference<List<Producto>>() {};

        try {
            // (5. Obtiene el archivo desde la carpeta 'resources')
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(RUTA_JSON);

            if (inputStream == null) {
                System.err.println("Error: No se pudo encontrar el archivo " + RUTA_JSON);
                return Collections.emptyList(); // Devuelve lista vacía
            }

            // (6. ¡La magia!) Lee el archivo y lo convierte en la lista de Productos
            List<Producto> productos = mapper.readValue(inputStream, typeReference);

            return productos;

        } catch (Exception e) {
            // (7. Manejo de errores básico)
            System.err.println("Error al leer el archivo JSON: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList(); // Devuelve lista vacía en caso de error
        }
    }

    /**
     * Esta es la Historia de Usuario: "Registrar Producto"
     * (La dejaremos para después)
     *
     * @param nuevoProducto El producto a añadir.
     * @return El producto guardado (aún no implementado).
     */
    public Producto crearProducto(Producto nuevoProducto) {
        // TODO: Implementar la lógica para AÑADIR al JSON
        System.out.println("LOGICA PENDIENTE: Guardar producto nuevo.");
        return nuevoProducto;
    }
}