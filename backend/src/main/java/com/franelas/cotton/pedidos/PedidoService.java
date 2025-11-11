package com.franelas.cotton.pedidos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.franelas.cotton.inventario.Producto;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PedidoService {

    private final String RUTA_PEDIDOS = "backend/backend/src/main/resources/data/pedidos.json";
    private final String RUTA_PRODUCTOS = "backend/backend/src/main/resources/data/productos.json";
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Consulta todos los pedidos
     */
    public List<Pedido> obtenerTodosLosPedidos() {
        try {
            File archivo = new File(RUTA_PEDIDOS);
            if (!archivo.exists() || archivo.length() == 0) {
                return Collections.emptyList();
            }
            return mapper.readValue(archivo, new TypeReference<List<Pedido>>() {});
        } catch (Exception e) {
            System.err.println("Error al leer pedidos: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Registra un nuevo pedido y actualiza el stock del producto correspondiente
     */
    public boolean registrarPedido(Pedido nuevoPedido) {
        try {
            // Leer pedidos existentes
            File filePedidos = new File(RUTA_PEDIDOS);
            List<Pedido> pedidos;
            if (filePedidos.exists() && filePedidos.length() > 0) {
                pedidos = mapper.readValue(filePedidos, new TypeReference<List<Pedido>>() {});
            } else {
                pedidos = new ArrayList<>();
            }

            // Generar ID automático
            if (nuevoPedido.getId() == 0) {
                long nextId = pedidos.stream()
                        .mapToLong(Pedido::getId)
                        .max()
                        .orElse(0) + 1;
                nuevoPedido.setId(nextId);
            }

            // Calcular total
            nuevoPedido.setTotal(nuevoPedido.getCantidad() * nuevoPedido.getPrecioUnitario());

            // Leer productos
            File fileProductos = new File(RUTA_PRODUCTOS);
            if (!fileProductos.exists() || fileProductos.length() == 0) {
                System.err.println("No existe el archivo de productos.");
                return false;
            }

            List<Producto> productos = mapper.readValue(fileProductos, new TypeReference<List<Producto>>() {});
            Producto productoSeleccionado = null;

            for (Producto p : productos) {
                if (p.getId() == nuevoPedido.getIdProducto()) {
                    productoSeleccionado = p;
                    break;
                }
            }

            if (productoSeleccionado == null) {
                System.err.println("Producto no encontrado.");
                return false;
            }

            // Validar stock
            if (productoSeleccionado.getStock() < nuevoPedido.getCantidad()) {
                System.err.println("Stock insuficiente para completar el pedido.");
                return false;
            }

            // Actualizar stock
            productoSeleccionado.setStock(productoSeleccionado.getStock() - nuevoPedido.getCantidad());
            mapper.writerWithDefaultPrettyPrinter().writeValue(fileProductos, productos);

            // Guardar pedido
            pedidos.add(nuevoPedido);
            mapper.writerWithDefaultPrettyPrinter().writeValue(filePedidos, pedidos);

            System.out.println("Pedido registrado con ID: " + nuevoPedido.getId());
            System.out.println("Stock actualizado del producto: " + productoSeleccionado.getNombre());
            return true;

        } catch (Exception e) {
            System.err.println("Error al registrar pedido: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
