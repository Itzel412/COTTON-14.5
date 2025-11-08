// itzel puso esto para tomarlo de ejemplo

package com.franelas.cotton.inventario;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service; // (Importante para Spring)

import java.io.File;
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
     * Le entran los datos de un productos, busca el archivo json o lo crea y agrega el producto al archivo
     *
     * @param nuevoProducto El producto a añadir.
     * @return true si el producto se guardo, false si no.
     */
    public boolean registrarProducto(Producto nuevoProducto) {

        ObjectMapper mapper = new ObjectMapper();

        try{
            File jsonFile = new File(RUTA_JSON);
            List<Producto> productos;

            if (jsonFile.exists() && jsonFile.length() > 0) {
                TypeReference<List<Producto>> typeReference = new TypeReference<List<Producto>>() {};
                productos = mapper.readValue(jsonFile, typeReference);
            } else {

                //en caso de que el archivo no exista o este vacio
                productos = new java.util.ArrayList<>();
                System.err.println("Error al leer el archivo " + RUTA_JSON);
            }

            // Asegurar un id unico que no se repita, si el id es 0 ponemos el proximo disponible
            if (nuevoProducto.getId() == 0){
                long nextId = productos.stream().mapToLong(Producto::getId).max().orElse(0) + 1;
                nuevoProducto.setId(nextId);
            }

            // Agregar producto a la lista
            productos.add(nuevoProducto);

            // devolver al JSON
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile,productos);
            // writeValue, agarra la lista productos y la escribe en formato Jason
            //writerWithDefaultPrettyPrinter, es para que el jason se vea mas bonito a la hora de leerlo

            System.out.println("Producto" + nuevoProducto.getNombre() + " registrado exitosamente");
            return true;  // exito

        } catch (Exception e){
            System.err.println("Error al leer el archivo " + e.getMessage());
            e.printStackTrace();
            return false;  //  fallo
        }
    }

}