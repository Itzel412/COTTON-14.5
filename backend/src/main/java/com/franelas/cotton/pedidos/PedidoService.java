package com.franelas.cotton.pedidos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.franelas.cotton.inventario.Producto;
import com.franelas.cotton.inventario.ProductoService;
import org.springframework.stereotype.Service;

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

    // Límite de seguridad
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

        // Cargar inventario
        List<Producto> inventario = productoService.obtenerTodosLosProductos();
        if (inventario.isEmpty()) {
            System.err.println("❌ Error: Inventario vacío.");
            return false;
        }

        String codigoUnico = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        boolean exitoTotal = true;
        
        for (Pedido p : listaPedidos) {
            p.setCodigo(codigoUnico);
            // Pasamos el inventario para no recargarlo
            if (!procesarItemIndividual(p, inventario)) {
                exitoTotal = false;
            }
        }
        
        // Si al menos uno funcionó, guardamos los cambios de stock
        if (exitoTotal) {
            productoService.guardarProductos(inventario);
        }

        return exitoTotal;
    }

    private boolean procesarItemIndividual(Pedido p, List<Producto> inventario) {
        try {
            // 1. Validar Cantidad (Aquí usamos la variable que daba warning)
            if (p.getCantidad() <= 0 || p.getCantidad() > MAX_CANTIDAD) {
                System.err.println("❌ Cantidad inválida: " + p.getCantidad());
                return false;
            }

            // 2. BUSCAR PRODUCTO (Lógica flexible)
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

            // 3. Validar Stock
            if (productoEncontrado.getStock() < p.getCantidad()) return false;

            // 4. Actualizar Stock en memoria
            productoEncontrado.setStock(productoEncontrado.getStock() - p.getCantidad());

            // 5. Completar datos
            p.setIdProducto(productoEncontrado.getId());
            p.setPrecioUnitario(productoEncontrado.getPrecio());
            p.setTotal(p.getCantidad() * productoEncontrado.getPrecio());

            // 6. Guardar pedido
            guardarPedidoEnJson(p);

            return true;

        } catch (Exception e) {
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