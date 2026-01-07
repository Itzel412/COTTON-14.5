package com.franelas.cotton.inventario;

import org.springframework.web.bind.annotation.*;
import java.util.List;

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

    @DeleteMapping("/productos/{id}")
    public boolean eliminarProducto(@PathVariable("id") long id) {
        return productoService.eliminarProducto(id);
    }

    @PutMapping("/productos/{id}")
    public boolean editarProducto(@PathVariable("id") long id, @RequestBody Producto productoActualizado) {
        productoActualizado.setId(id);
        return productoService.editarProducto(productoActualizado);
    }
}
