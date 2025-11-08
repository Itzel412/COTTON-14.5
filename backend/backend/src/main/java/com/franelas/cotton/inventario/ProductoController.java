//itzel puso esto de ejemplo


package com.franelas.cotton.inventario;
import org.springframework.web.bind.annotation.*; // (1. Importa todas las anotaciones web)

import java.util.List;

@RestController // (2. Le dice a Spring: Esta clase es un Controlador REST, devuelve JSON)
@RequestMapping("/api/inventario") // (3. La URL base para todos los métodos de esta clase)
public class ProductoController {

    // (4. El controlador necesita al servicio para funcionar)
    private final ProductoService productoService;

    // (5. Inyección de Dependencias por Constructor - La mejor práctica)
    // Spring ve esto y automáticamente "inyecta" una instancia de ProductoService aquí.
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * HISTORIA DE USUARIO: "Consultar Productos"
     * Esto expone el método del servicio en una URL.
     * URL: GET http://localhost:8080/api/inventario/productos
     */
    @GetMapping("/productos") // (6. Mapea peticiones GET a esta URL)
    public List<Producto> consultarTodosLosProductos() {
        // (7. Llama al servicio para obtener los datos y los devuelve como JSON)
        return productoService.obtenerTodosLosProductos();
    }

    /**
     * HISTORIA DE USUARIO: "Registrar Producto"
     * (Lo dejaremos para después, pero ya creamos la URL)
     * URL: POST http://localhost:8080/api/inventario/productos
     */
    @PostMapping("/productos") // (8. Mapea peticiones POST a esta URL)
    public boolean registrarProducto(@RequestBody Producto nuevoProducto) {
        // (9. @RequestBody toma el JSON del frontend y lo convierte en un objeto Producto)
        return productoService.registrarProducto(nuevoProducto);
    }
}