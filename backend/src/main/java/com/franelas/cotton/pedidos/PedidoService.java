package com.franelas.cotton.pedidos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.franelas.cotton.inventario.Producto;
import com.franelas.cotton.inventario.ProductoService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class PedidoService {

    private final String RUTA_JSON = "src/main/resources/data/pedidos.json";
    private final ObjectMapper mapper = new ObjectMapper();

    private final ProductoService productoService;

    public PedidoService(ProductoService productoService) {
        this.productoService = productoService;
    }

    private static final int MAX_CANTIDAD = 10;

    private static final List<String> TALLAS_VALIDAS =
            Arrays.asList("S", "M", "L", "XL");

    private static final List<String> COLORES_VALIDOS =
            Arrays.asList("Blanco", "Negro", "Rojo", "Azul", "Amarillo", "Verde", "Morado");

    public List<Pedido> obtenerTodosLosPedidos() {
        try {
            File jsonFile = new File(RUTA_JSON);
            if (!jsonFile.exists() || jsonFile.length() == 0) {
                System.err.println("No hay pedidos registrados aún.");
                return Collections.emptyList();
            }
            return mapper.readValue(jsonFile, new TypeReference<List<Pedido>>() {});
        } catch (Exception e) {
            System.err.println("Error al leer el archivo de pedidos: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    public boolean registrarPedido(Pedido nuevoPedido) {
        try {
            System.out.println("=== Intentando registrar pedido ===");
            System.out.println("Usuario: " + nuevoPedido.getUsuario());
            System.out.println("Color recibido: " + nuevoPedido.getColor());
            System.out.println("Talla recibida: " + nuevoPedido.getTalla());
            System.out.println("Cantidad recibida: " + nuevoPedido.getCantidad());
            System.out.println("idProducto recibido: " + nuevoPedido.getIdProducto());

            int cantidad = nuevoPedido.getCantidad();
            if (cantidad <= 0 || cantidad > MAX_CANTIDAD) {
                System.err.println("Cantidad inválida: " + cantidad);
                return false;
            }

            String talla = nuevoPedido.getTalla();
            if (talla == null) {
                System.err.println("Talla nula");
                return false;
            }
            String tallaUpper = talla.toUpperCase();
            if (!TALLAS_VALIDAS.contains(tallaUpper)) {
                System.err.println("Talla inválida: " + talla);
                return false;
            }
            nuevoPedido.setTalla(tallaUpper);

            String color = nuevoPedido.getColor();
            if (color == null) {
                System.err.println("Color nulo");
                return false;
            }

            String colorNormalizado = COLORES_VALIDOS.stream()
                    .filter(c -> c.equalsIgnoreCase(color))
                    .findFirst()
                    .orElse(null);

            if (colorNormalizado == null) {
                System.err.println("Color inválido: " + color);
                return false;
            }
            nuevoPedido.setColor(colorNormalizado);

            List<Producto> productos = productoService.obtenerTodosLosProductos();
            if (productos.isEmpty()) {
                System.err.println("No hay productos en inventario.");
                return false;
            }

            Producto producto = null;
            if (nuevoPedido.getIdProducto() != 0) {
                long idBuscado = nuevoPedido.getIdProducto();
                producto = productos.stream()
                        .filter(p -> p.getId() == idBuscado)
                        .findFirst()
                        .orElse(null);
            }

            if (producto == null) {
                producto = productos.stream()
                        .filter(p ->
                                p.getColor() != null &&
                                        p.getTalla() != null &&
                                        p.getColor().equalsIgnoreCase(colorNormalizado) &&
                                        p.getTalla().equalsIgnoreCase(tallaUpper)
                        )
                        .findFirst()
                        .orElse(null);
            }

            if (producto == null) {
                System.err.println(
                        "No existe producto con id " + nuevoPedido.getIdProducto() +
                                " o con combinación color/talla " + colorNormalizado + " - " + tallaUpper
                );
                return false;
            }

            System.out.println("Producto encontrado en inventario: id=" + producto.getId()
                    + ", color=" + producto.getColor()
                    + ", talla=" + producto.getTalla()
                    + ", stock actual=" + producto.getStock());

            if (cantidad > producto.getStock()) {
                System.err.println("Stock insuficiente para el producto (id="
                        + producto.getId() + "), stock=" + producto.getStock()
                        + ", cantidad solicitada=" + cantidad);
                return false;
            }

            int stockNuevo = producto.getStock() - cantidad;
            producto.setStock(stockNuevo);

            System.out.println("Nuevo stock para producto id=" + producto.getId()
                    + ": " + stockNuevo);

            boolean inventarioOk = productoService.guardarProductos(productos);
            if (!inventarioOk) {
                System.err.println("No se pudo actualizar el inventario. Pedido cancelado.");
                return false;
            }

            nuevoPedido.setPrecioUnitario(producto.getPrecio());
            nuevoPedido.setTotal(cantidad * producto.getPrecio());
            nuevoPedido.setIdProducto(producto.getId()); // por si vino en 0

            File jsonFile = new File(RUTA_JSON);
            List<Pedido> pedidos;

            if (jsonFile.exists() && jsonFile.length() > 0) {
                pedidos = mapper.readValue(jsonFile, new TypeReference<List<Pedido>>() {});
            } else {
                pedidos = new ArrayList<>();
            }

            if (nuevoPedido.getId() == 0) {
                long nextId = pedidos.stream()
                        .mapToLong(Pedido::getId)
                        .max()
                        .orElse(0) + 1;
                nuevoPedido.setId(nextId);
            }

            pedidos.add(nuevoPedido);
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, pedidos);

            System.out.println("Pedido registrado exitosamente con ID: " + nuevoPedido.getId());
            return true;

        } catch (Exception e) {
            System.err.println("Error al registrar pedido: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarPedido(Pedido pedidoActualizado) {
        try {
            System.out.println("=== Intentando actualizar pedido ID: " + pedidoActualizado.getId() + " ===");

            int nuevaCantidad = pedidoActualizado.getCantidad();
            if (nuevaCantidad <= 0 || nuevaCantidad > MAX_CANTIDAD) {
                System.err.println("Cantidad inválida para actualización: " + nuevaCantidad);
                return false;
            }

            String talla = pedidoActualizado.getTalla();
            if (talla == null || !TALLAS_VALIDAS.contains(talla.toUpperCase())) {
                System.err.println("Talla inválida: " + talla);
                return false;
            }
            String tallaUpper = talla.toUpperCase();

            String color = pedidoActualizado.getColor();
            String colorNormalizado = COLORES_VALIDOS.stream()
                    .filter(c -> c.equalsIgnoreCase(color))
                    .findFirst()
                    .orElse(null);

            if (colorNormalizado == null) {
                System.err.println("Color inválido: " + color);
                return false;
            }

            File jsonFile = new File(RUTA_JSON);
            List<Pedido> pedidos = new ArrayList<>();
            if (jsonFile.exists() && jsonFile.length() > 0) {
                pedidos = mapper.readValue(jsonFile, new TypeReference<List<Pedido>>() {});
            }

            Pedido pedidoAnterior = pedidos.stream()
                    .filter(p -> p.getId() == pedidoActualizado.getId())
                    .findFirst()
                    .orElse(null);

            if (pedidoAnterior == null) {
                System.err.println("No se encontró el pedido con ID: " + pedidoActualizado.getId());
                return false;
            }

            List<Producto> productos = productoService.obtenerTodosLosProductos();


            Producto productoAnterior = productos.stream()
                    .filter(p -> p.getId() == pedidoAnterior.getIdProducto())
                    .findFirst()
                    .orElse(null);

            if (productoAnterior != null) {
                productoAnterior.setStock(productoAnterior.getStock() + pedidoAnterior.getCantidad());
                System.out.println("Stock restaurado temporalmente para producto ID " + productoAnterior.getId());
            } else {
                System.err.println("Advertencia: El producto original ya no existe en la base de datos.");
            }
            Producto productoNuevo = null;

            if (pedidoActualizado.getIdProducto() != 0) {
                long idBuscado = pedidoActualizado.getIdProducto();
                productoNuevo = productos.stream().filter(p -> p.getId() == idBuscado).findFirst().orElse(null);
            }

            if (productoNuevo == null) {
                productoNuevo = productos.stream()
                        .filter(p -> p.getColor().equalsIgnoreCase(colorNormalizado) &&
                                p.getTalla().equalsIgnoreCase(tallaUpper))
                        .findFirst()
                        .orElse(null);
            }

            if (productoNuevo == null) {
                System.err.println("No existe producto para la combinación: " + colorNormalizado + " / " + tallaUpper);
                return false;
            }

            if (productoNuevo.getStock() < nuevaCantidad) {
                System.err.println("Stock insuficiente tras actualización. Stock disponible: " +
                        productoNuevo.getStock() + ", Solicitado: " + nuevaCantidad);
                return false;
            }

            productoNuevo.setStock(productoNuevo.getStock() - nuevaCantidad);

            boolean inventarioActualizado = productoService.guardarProductos(productos);
            if (!inventarioActualizado) {
                System.err.println("Error al guardar el inventario. Cancelando actualización.");
                return false;
            }

            pedidoAnterior.setTalla(tallaUpper);
            pedidoAnterior.setColor(colorNormalizado);
            pedidoAnterior.setCantidad(nuevaCantidad);
            pedidoAnterior.setIdProducto(productoNuevo.getId());
            pedidoAnterior.setPrecioUnitario(productoNuevo.getPrecio());
            pedidoAnterior.setTotal(productoNuevo.getPrecio() * nuevaCantidad);
            if(pedidoActualizado.getUsuario() != null) {
                pedidoAnterior.setUsuario(pedidoActualizado.getUsuario());
            }

            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, pedidos);

            System.out.println("Pedido actualizado exitosamente. Nuevo Total: " + pedidoAnterior.getTotal());
            return true;

        } catch (Exception e) {
            System.err.println("Error al actualizar pedido: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
