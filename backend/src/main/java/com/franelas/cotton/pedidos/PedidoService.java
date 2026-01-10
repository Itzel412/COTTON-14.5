package com.franelas.cotton.pedidos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.franelas.cotton.inventario.Producto;
import com.franelas.cotton.inventario.ProductoService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final String RUTA_FOLDER = "data";
    private final String RUTA_JSON = RUTA_FOLDER + "/pedidos.json";

    private final ObjectMapper mapper = new ObjectMapper();
    private final ProductoService productoService;

    public PedidoService(ProductoService productoService) {
        this.productoService = productoService;
    }

    private static final List<String> TALLAS_VALIDAS = Arrays.asList("S", "M", "L", "XL");

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
        try {
            if (listaPedidos == null || listaPedidos.isEmpty()) return false;

            List<Producto> inventario = productoService.obtenerTodosLosProductos();
            if (inventario == null || inventario.isEmpty()) {
                System.err.println("❌ Error: Inventario vacío.");
                return false;
            }

            List<Pedido> existentes = leerPedidosDesdeJson();
            long nextId = existentes.stream().mapToLong(Pedido::getId).max().orElse(0) + 1;

            String codigoUnico = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String hoy = LocalDate.now().toString();

            Map<Long, Producto> mapProductos = inventario.stream()
                    .collect(Collectors.toMap(Producto::getId, p -> p));

            List<Pedido> nuevosPedidosProcesados = new ArrayList<>();

            for (Pedido p : listaPedidos) {
                if (p == null) return false;

                if (p.getCantidad() <= 0 || p.getCantidad() > MAX_CANTIDAD) {
                    System.err.println("Cantidad inválida: " + p.getCantidad());
                    return false;
                }

                String talla = p.getTalla() != null ? p.getTalla().trim().toUpperCase() : "";
                if (!TALLAS_VALIDAS.contains(talla)) {
                    System.err.println("Talla inválida: " + p.getTalla());
                    return false;
                }

                String color = p.getColor() != null ? p.getColor().trim() : "";
                String colorNormalizado = COLORES_VALIDOS.stream()
                        .filter(c -> c.equalsIgnoreCase(color))
                        .findFirst()
                        .orElse(null);
                if (colorNormalizado == null) {
                    System.err.println("Color inválido: " + p.getColor());
                    return false;
                }

                Producto productoEncontrado = null;

                if (p.getIdProducto() > 0) {
                    productoEncontrado = mapProductos.get(p.getIdProducto());
                }

                if (productoEncontrado == null) {
                    for (Producto prod : inventario) {
                        String prodColor = prod.getColor() != null ? prod.getColor().trim() : "";
                        String prodTalla = prod.getTalla() != null ? prod.getTalla().trim().toUpperCase() : "";
                        if (prodColor.equalsIgnoreCase(colorNormalizado) && prodTalla.equalsIgnoreCase(talla)) {
                            productoEncontrado = prod;
                            break;
                        }
                    }
                }

                if (productoEncontrado == null) {
                    System.err.println("Producto no encontrado para item.");
                    return false;
                }

                if (productoEncontrado.getStock() < p.getCantidad()) {
                    System.err.println("Stock insuficiente para producto id=" + productoEncontrado.getId());
                    return false;
                }

                productoEncontrado.setStock(productoEncontrado.getStock() - p.getCantidad());

                Pedido nuevo = new Pedido();
                nuevo.setId(nextId++);
                nuevo.setCodigo(codigoUnico);
                nuevo.setFecha(p.getFecha() != null && !p.getFecha().trim().isEmpty() ? p.getFecha() : hoy);

                nuevo.setUsuario(p.getUsuario());
                nuevo.setIdProducto(productoEncontrado.getId());
                nuevo.setColor(colorNormalizado);
                nuevo.setTalla(talla);
                nuevo.setCantidad(p.getCantidad());
                nuevo.setPrecioUnitario(productoEncontrado.getPrecio());
                nuevo.setTotal(p.getCantidad() * productoEncontrado.getPrecio());

                nuevosPedidosProcesados.add(nuevo);
            }

            if (!productoService.guardarProductos(inventario)) {
                System.err.println("❌ Error: No se pudo guardar inventario.");
                return false;
            }

            existentes.addAll(nuevosPedidosProcesados);
            escribirPedidosEnJson(existentes);

            return true;

        } catch (Exception e) {
            System.err.println("Error al registrar pedidos: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarPedido(Pedido pedidoActualizado) {
        try {
            if (pedidoActualizado == null || pedidoActualizado.getId() <= 0) return false;

            int nuevaCantidad = pedidoActualizado.getCantidad();
            if (nuevaCantidad <= 0 || nuevaCantidad > MAX_CANTIDAD) return false;

            String talla = pedidoActualizado.getTalla() != null ? pedidoActualizado.getTalla().trim().toUpperCase() : "";
            if (!TALLAS_VALIDAS.contains(talla)) return false;

            String color = pedidoActualizado.getColor() != null ? pedidoActualizado.getColor().trim() : "";
            String colorNormalizado = COLORES_VALIDOS.stream()
                    .filter(c -> c.equalsIgnoreCase(color))
                    .findFirst()
                    .orElse(null);
            if (colorNormalizado == null) return false;

            List<Pedido> pedidos = leerPedidosDesdeJson();
            Pedido pedidoAnterior = pedidos.stream()
                    .filter(p -> p.getId() == pedidoActualizado.getId())
                    .findFirst()
                    .orElse(null);

            if (pedidoAnterior == null) return false;

            List<Producto> productos = productoService.obtenerTodosLosProductos();

            Producto productoAnterior = productos.stream()
                    .filter(p -> p.getId() == pedidoAnterior.getIdProducto())
                    .findFirst()
                    .orElse(null);
            if (productoAnterior != null) {
                productoAnterior.setStock(productoAnterior.getStock() + pedidoAnterior.getCantidad());
            }

            Producto productoNuevo = null;

            if (pedidoActualizado.getIdProducto() > 0) {
                long idBuscado = pedidoActualizado.getIdProducto();
                productoNuevo = productos.stream().filter(p -> p.getId() == idBuscado).findFirst().orElse(null);
            }

            if (productoNuevo == null) {
                productoNuevo = productos.stream()
                        .filter(p -> p.getColor().equalsIgnoreCase(colorNormalizado)
                                && p.getTalla().equalsIgnoreCase(talla))
                        .findFirst()
                        .orElse(null);
            }

            if (productoNuevo == null) return false;

            if (productoNuevo.getStock() < nuevaCantidad) return false;

            productoNuevo.setStock(productoNuevo.getStock() - nuevaCantidad);

            if (!productoService.guardarProductos(productos)) return false;

            pedidoAnterior.setTalla(talla);
            pedidoAnterior.setColor(colorNormalizado);
            pedidoAnterior.setCantidad(nuevaCantidad);
            pedidoAnterior.setIdProducto(productoNuevo.getId());
            pedidoAnterior.setPrecioUnitario(productoNuevo.getPrecio());
            pedidoAnterior.setTotal(productoNuevo.getPrecio() * nuevaCantidad);

            if (pedidoActualizado.getUsuario() != null) pedidoAnterior.setUsuario(pedidoActualizado.getUsuario());
            if (pedidoActualizado.getFecha() != null) pedidoAnterior.setFecha(pedidoActualizado.getFecha());

            escribirPedidosEnJson(pedidos);
            return true;

        } catch (Exception e) {
            System.err.println("Error al actualizar pedido: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarPedido(long idPedido) {
        try {
            List<Pedido> pedidos = leerPedidosDesdeJson();
            Pedido pedidoAEliminar = pedidos.stream()
                    .filter(p -> p.getId() == idPedido)
                    .findFirst()
                    .orElse(null);
            if (pedidoAEliminar == null) return false;

            List<Producto> productos = productoService.obtenerTodosLosProductos();
            Producto productoAsociado = productos.stream()
                    .filter(p -> p.getId() == pedidoAEliminar.getIdProducto())
                    .findFirst()
                    .orElse(null);

            if (productoAsociado != null) {
                productoAsociado.setStock(productoAsociado.getStock() + pedidoAEliminar.getCantidad());
                if (!productoService.guardarProductos(productos)) return false;
            }

            pedidos.remove(pedidoAEliminar);
            escribirPedidosEnJson(pedidos);
            return true;

        } catch (Exception e) {
            System.err.println("Error al eliminar el pedido: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarPedidosPorCodigo(String codigo) {
        try {
            if (codigo == null || codigo.trim().isEmpty()) return false;

            List<Pedido> pedidos = leerPedidosDesdeJson();
            List<Pedido> aEliminar = pedidos.stream()
                    .filter(p -> codigo.equalsIgnoreCase(p.getCodigo()))
                    .collect(Collectors.toList());

            if (aEliminar.isEmpty()) return false;

            List<Producto> productos = productoService.obtenerTodosLosProductos();

            for (Pedido it : aEliminar) {
                Producto prod = productos.stream()
                        .filter(p -> p.getId() == it.getIdProducto())
                        .findFirst()
                        .orElse(null);
                if (prod != null) {
                    prod.setStock(prod.getStock() + it.getCantidad());
                }
            }

            if (!productoService.guardarProductos(productos)) return false;

            pedidos.removeAll(aEliminar);
            escribirPedidosEnJson(pedidos);

            return true;

        } catch (Exception e) {
            System.err.println("Error al eliminar por código: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private List<Pedido> leerPedidosDesdeJson() {
        try {
            File jsonFile = new File(RUTA_JSON);
            if (!jsonFile.exists() || jsonFile.length() == 0) return new ArrayList<>();
            return mapper.readValue(jsonFile, new TypeReference<List<Pedido>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void escribirPedidosEnJson(List<Pedido> pedidos) throws Exception {
        File folder = new File(RUTA_FOLDER);
        if (!folder.exists()) folder.mkdirs();

        File jsonFile = new File(RUTA_JSON);
        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, pedidos);
    }
}
