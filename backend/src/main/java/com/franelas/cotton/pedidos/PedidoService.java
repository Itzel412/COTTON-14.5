package com.franelas.cotton.pedidos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.franelas.cotton.inventario.Producto;
import com.franelas.cotton.inventario.ProductoService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PedidoService {

    private final String RUTA_JSON = "backend/backend/src/main/resources/data/pedidos.json";
    private final ObjectMapper mapper = new ObjectMapper();

    private final ProductoService productoService;

    public PedidoService(ProductoService productoService) {
        this.productoService = productoService;
    }

    private static final int MAX_CANTIDAD = 10;

    private static final List<String> TALLAS_VALIDAS =
            List.of("S", "M", "L", "XL");

    private static final List<String> COLORES_VALIDOS =
            List.of("Blanco", "Negro", "Rojo", "Azul", "Amarillo", "Verde", "Morado");


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
            return Collections.emptyList();
        }
    }

    public boolean registrarPedido(Pedido nuevoPedido) {
        try {
            if (nuevoPedido.getCantidad() <= 0 || nuevoPedido.getCantidad() > MAX_CANTIDAD) {
                System.err.println("Cantidad inválida: " + nuevoPedido.getCantidad());
                return false;
            }

            if (nuevoPedido.getTalla() == null ||
                    !TALLAS_VALIDAS.contains(nuevoPedido.getTalla().toUpperCase())) {
                System.err.println("Talla inválida: " + nuevoPedido.getTalla());
                return false;
            }

            if (nuevoPedido.getColor() == null ||
                    !COLORES_VALIDOS.contains(nuevoPedido.getColor())) {
                System.err.println("Color inválido: " + nuevoPedido.getColor());
                return false;
            }

            List<Producto> productos = productoService.obtenerTodosLosProductos();
            Producto producto = productos.stream()
                    .filter(p -> p.getId() == nuevoPedido.getIdProducto())
                    .findFirst()
                    .orElse(null);

            if (producto == null) {
                System.err.println("No existe producto con id " + nuevoPedido.getIdProducto());
                return false;
            }

            if (nuevoPedido.getCantidad() > producto.getStock()) {
                System.err.println("Stock insuficiente para el producto " + producto.getNombre());
                return false;
            }

            int stockNuevo = producto.getStock() - nuevoPedido.getCantidad();
            producto.setStock(stockNuevo);

            boolean inventarioOk = productoService.guardarProductos(productos);
            if (!inventarioOk) {
                System.err.println("No se pudo actualizar el inventario. Pedido cancelado.");
                return false;
            }

            nuevoPedido.setNombre(producto.getNombre());
            nuevoPedido.setColor(producto.getColor());
            nuevoPedido.setTalla(producto.getTalla());
            nuevoPedido.setPrecioUnitario(producto.getPrecio());

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

            nuevoPedido.setTotal(nuevoPedido.getCantidad() * nuevoPedido.getPrecioUnitario());

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
}
