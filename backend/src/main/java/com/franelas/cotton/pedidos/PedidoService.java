package com.franelas.cotton.pedidos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.franelas.cotton.facturas.Factura;
import com.franelas.cotton.inventario.Producto;
import com.franelas.cotton.inventario.ProductoService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ProductoService productoService;

    private final Path pedidosJsonPath;
    private final Path facturasJsonPath; // NUEVO: ruta real a facturas.json (sin copias)

    private static final List<String> TALLAS_VALIDAS = Arrays.asList("S", "M", "L", "XL");

    private static final List<String> COLORES_VALIDOS =
            Arrays.asList("Blanco", "Negro", "Rojo", "Azul", "Amarillo", "Verde", "Morado");

    private static final int MAX_CANTIDAD = 100;

    public PedidoService(ProductoService productoService) {
        this.productoService = productoService;

        this.pedidosJsonPath = resolvePedidosJsonPath();
        ensureParentDirExists(this.pedidosJsonPath);

        this.facturasJsonPath = resolveFacturasJsonPath();
        ensureParentDirExists(this.facturasJsonPath);

        System.out.println("PedidoService usando pedidos.json en: " + this.pedidosJsonPath.toAbsolutePath());
        System.out.println("PedidoService usando facturas.json en: " + this.facturasJsonPath.toAbsolutePath());
    }

    private Path resolvePedidosJsonPath() {
        Path base = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();

        Path[] candidates = new Path[] {
                base.resolve("src/main/resources/data/pedidos.json"),
                base.resolve("backend/src/main/resources/data/pedidos.json"),
                base.resolve("..").resolve("backend/src/main/resources/data/pedidos.json").normalize()
        };

        for (Path p : candidates) {
            if (Files.exists(p)) return p;
        }
        return candidates[0];
    }

    private Path resolveFacturasJsonPath() {
        Path base = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();

        Path[] candidates = new Path[] {
                base.resolve("src/main/resources/data/facturas.json"),
                base.resolve("backend/src/main/resources/data/facturas.json"),
                base.resolve("..").resolve("backend/src/main/resources/data/facturas.json").normalize()
        };

        for (Path p : candidates) {
            if (Files.exists(p)) return p;
        }
        return candidates[0];
    }

    private void ensureParentDirExists(Path filePath) {
        try {
            Path parent = filePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(filePath)) {
                mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), new ArrayList<>());
            }
        } catch (Exception e) {
            throw new RuntimeException("No se pudo preparar JSON en: " + filePath, e);
        }
    }

    private File getJsonFile() {
        return pedidosJsonPath.toFile();
    }

    private File getFacturasFile() {
        return facturasJsonPath.toFile();
    }

    private boolean codigoPedidoEstaFacturado(String codigoPedido) {
        try {
            if (codigoPedido == null || codigoPedido.trim().isEmpty()) return false;

            File jsonFile = getFacturasFile();
            if (!jsonFile.exists() || jsonFile.length() == 0) return false;

            List<Factura> facturas = mapper.readValue(jsonFile, new TypeReference<List<Factura>>() {});
            return facturas.stream().anyMatch(f ->
                    f.getCodigoPedido() != null && f.getCodigoPedido().equalsIgnoreCase(codigoPedido.trim())
            );
        } catch (Exception e) {
            System.err.println("Aviso: No se pudo leer facturas.json para bloqueo: " + e.getMessage());
            return false;
        }
    }

    public List<Pedido> obtenerTodosLosPedidos() {
        try {
            File jsonFile = getJsonFile();
            if (!jsonFile.exists() || jsonFile.length() == 0) return Collections.emptyList();
            return mapper.readValue(jsonFile, new TypeReference<List<Pedido>>() {});
        } catch (Exception e) {
            System.err.println("Aviso: No se pudo leer pedidos.json: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean registrarMultiplesPedidos(List<Pedido> listaPedidos) {
        try {
            if (listaPedidos == null || listaPedidos.isEmpty()) return false;

            List<Producto> inventario = productoService.obtenerTodosLosProductos();
            if (inventario == null || inventario.isEmpty()) {
                System.err.println(" Error: Inventario vacío.");
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

                if (p.getCantidad() <= 0 || p.getCantidad() > MAX_CANTIDAD) return false;

                String talla = p.getTalla() != null ? p.getTalla().trim().toUpperCase() : "";
                if (!TALLAS_VALIDAS.contains(talla)) return false;

                String color = p.getColor() != null ? p.getColor().trim() : "";
                String colorNormalizado = COLORES_VALIDOS.stream()
                        .filter(c -> c.equalsIgnoreCase(color))
                        .findFirst()
                        .orElse(null);
                if (colorNormalizado == null) return false;

                Producto productoEncontrado = null;

                if (p.getIdProducto() > 0) productoEncontrado = mapProductos.get(p.getIdProducto());

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

                if (productoEncontrado == null) return false;
                if (productoEncontrado.getStock() < p.getCantidad()) return false;

                productoEncontrado.setStock(productoEncontrado.getStock() - p.getCantidad());

                Pedido nuevo = new Pedido();
                nuevo.setId(nextId++);
                nuevo.setCodigo(codigoUnico);
                nuevo.setFecha((p.getFecha() != null && !p.getFecha().trim().isEmpty()) ? p.getFecha() : hoy);

                nuevo.setUsuario(p.getUsuario());
                nuevo.setIdProducto(productoEncontrado.getId());
                nuevo.setColor(colorNormalizado);
                nuevo.setTalla(talla);
                nuevo.setCantidad(p.getCantidad());
                nuevo.setPrecioUnitario(productoEncontrado.getPrecio());
                nuevo.setTotal(p.getCantidad() * productoEncontrado.getPrecio());

                nuevosPedidosProcesados.add(nuevo);
            }

            if (!productoService.guardarProductos(inventario)) return false;

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

            if (codigoPedidoEstaFacturado(pedidoAnterior.getCodigo())) {
                System.err.println(" No se puede editar un pedido ya facturado: " + pedidoAnterior.getCodigo());
                return false;
            }

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

            if (codigoPedidoEstaFacturado(pedidoAEliminar.getCodigo())) {
                System.err.println("No se puede eliminar un item de un pedido ya facturado: " + pedidoAEliminar.getCodigo());
                return false;
            }

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

            if (codigoPedidoEstaFacturado(codigo)) {
                System.err.println("No se puede eliminar un pedido ya facturado: " + codigo);
                return false;
            }

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
                if (prod != null) prod.setStock(prod.getStock() + it.getCantidad());
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
            File jsonFile = getJsonFile();
            if (!jsonFile.exists() || jsonFile.length() == 0) return new ArrayList<>();
            return mapper.readValue(jsonFile, new TypeReference<List<Pedido>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void escribirPedidosEnJson(List<Pedido> pedidos) throws Exception {
        mapper.writerWithDefaultPrettyPrinter().writeValue(getJsonFile(), pedidos);
    }
}
