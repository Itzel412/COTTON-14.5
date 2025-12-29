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
import java.util.UUID;

@Service
public class PedidoService {

    // CAMBIO IMPORTANTE: Guardamos en la raíz del proyecto para asegurar persistencia
    private final String RUTA_FOLDER = "data"; 
    private final String RUTA_JSON = RUTA_FOLDER + "/pedidos.json";
    
    private final ObjectMapper mapper = new ObjectMapper();
    private final ProductoService productoService;

    public PedidoService(ProductoService productoService) {
        this.productoService = productoService;
    }

    private static final List<String> TALLAS_VALIDAS =
            Arrays.asList("S", "M", "L", "XL");

    private static final List<String> COLORES_VALIDOS =
            Arrays.asList("Blanco", "Negro", "Rojo", "Azul", "Amarillo", "Verde", "Morado");


    private static final int MAX_CANTIDAD = 100;

    public List<Pedido> obtenerTodosLosPedidos() {
        try {
            File jsonFile = new File(RUTA_JSON);
            if (!jsonFile.exists() || jsonFile.length() == 0) return Collections.emptyList();
            return mapper.readValue(jsonFile, new TypeReference<List<Pedido>>() {});
        } catch (Exception e) {
            System.err.println("Aviso: No se pudo leer el historial de pedidos (puede estar vacío).");
            return Collections.emptyList();
        }
    }

    public boolean registrarMultiplesPedidos(List<Pedido> listaPedidos) {
        if (listaPedidos == null || listaPedidos.isEmpty()) return false;

        List<Producto> inventario = productoService.obtenerTodosLosProductos();
        if (inventario.isEmpty()) {
            System.err.println("❌ Error: Inventario vacío.");
            return false;
        }

        String codigoUnico = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        boolean exitoTotal = true;
        
        for (Pedido p : listaPedidos) {
            p.setCodigo(codigoUnico);
            if (!procesarItemIndividual(p, inventario)) {
                exitoTotal = false;
            }
        }

        if (exitoTotal) {
            productoService.guardarProductos(inventario);
        }

        return exitoTotal;
    }

    private boolean procesarItemIndividual(Pedido p, List<Producto> inventario) {
        try {
            if (p.getCantidad() <= 0 || p.getCantidad() > MAX_CANTIDAD) {
                System.err.println("❌ Cantidad inválida: " + p.getCantidad());
                return false;
            }

            Producto productoEncontrado = null;

            if (p.getIdProducto() > 0) {
                productoEncontrado = inventario.stream()
                        .filter(prod -> prod.getId() == p.getIdProducto())
                        .findFirst().orElse(null);
            }

            if (productoEncontrado == null) {
                String colorBuscado = p.getColor() != null ? p.getColor().trim() : "";
                String tallaBuscada = p.getTalla() != null ? p.getTalla().trim() : "";

                for (Producto prod : inventario) {
                    String prodColor = prod.getColor() != null ? prod.getColor().trim() : "";
                    String prodTalla = prod.getTalla() != null ? prod.getTalla().trim() : "";

                    if (prodColor.equalsIgnoreCase(colorBuscado) && prodTalla.equalsIgnoreCase(tallaBuscada)) {
                        productoEncontrado = prod;
                        break;
                    }
                }
            }

            if (productoEncontrado == null) return false;

            if (productoEncontrado.getStock() < p.getCantidad()) return false;

            productoEncontrado.setStock(productoEncontrado.getStock() - p.getCantidad());

            p.setIdProducto(productoEncontrado.getId());
            p.setPrecioUnitario(productoEncontrado.getPrecio());
            p.setTotal(p.getCantidad() * productoEncontrado.getPrecio());

            guardarPedidoEnJson(p);

            return true;

        } catch (Exception e) {
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

    private void guardarPedidoEnJson(Pedido nuevoPedido) {
        try {
            File folder = new File(RUTA_FOLDER);
            if (!folder.exists()) folder.mkdirs(); // Crea la carpeta "data" si no existe

            File jsonFile = new File(RUTA_JSON);
            List<Pedido> pedidos = new ArrayList<>();
            
            if (jsonFile.exists() && jsonFile.length() > 0) {
                pedidos = new ArrayList<>(Arrays.asList(mapper.readValue(jsonFile, Pedido[].class)));
            }

            long nextId = pedidos.stream().mapToLong(Pedido::getId).max().orElse(0) + 1;
            nuevoPedido.setId(nextId);

            pedidos.add(nuevoPedido);
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, pedidos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
