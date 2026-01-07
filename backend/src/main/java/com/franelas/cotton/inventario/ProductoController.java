package com.franelas.cotton.inventario;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://127.0.0.1:5173",
        "http://localhost:5174",
        "http://127.0.0.1:5174"
})
@RestController
@RequestMapping("/api/inventario")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/productos")
    public List<Producto> consultarTodosLosProductos() {
        return productoService.obtenerTodosLosProductos();
    }

    @PostMapping("/productos")
    public boolean registrarProducto(@RequestBody Producto nuevoProducto) {
        return productoService.registrarProducto(nuevoProducto);
    }

    @PutMapping("/productos/{id}")
    public boolean editarProducto(@PathVariable long id, @RequestBody Producto productoActualizado) {
        productoActualizado.setId(id);
        return productoService.editarProducto(productoActualizado);
    }

    @DeleteMapping("/productos/{id}")
    public boolean eliminarProducto(@PathVariable long id) {
        return productoService.eliminarProducto(id);
    }
}
