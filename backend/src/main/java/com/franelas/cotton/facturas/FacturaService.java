package com.franelas.cotton.facturas;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.franelas.cotton.pedidos.Pedido;
import com.franelas.cotton.pedidos.PedidoService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacturaService {

    private static final double IVA_TASA = 0.16;
    private final String RUTA_JSON = "data/facturas.json";
    private final ObjectMapper mapper = new ObjectMapper();
    private final PedidoService pedidoService;

    public FacturaService(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
        crearArchivoSiNoExiste();
    }

    private void crearArchivoSiNoExiste() {
        try {
            File file = new File(RUTA_JSON);
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            if (!file.exists()) mapper.writeValue(file, new ArrayList<>());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public List<Factura> obtenerTodasLasFacturas() {
        try {
            File jsonFile = new File(RUTA_JSON);
            if (!jsonFile.exists()) return new ArrayList<>();
            return mapper.readValue(jsonFile, new TypeReference<List<Factura>>() {});
        } catch (Exception e) { return new ArrayList<>(); }
    }

    public boolean registrarFactura(Factura datosEntrada) {
        try {
            // El ID que viene aquí es el ID del pedido que seleccionó el usuario
            long idReferencia = datosEntrada.getId();
            
            List<Pedido> pedidos = pedidoService.obtenerTodosLosPedidos();
            
            // 1. Buscamos el pedido para obtener su CÓDIGO DE GRUPO
            Pedido pedidoRef = pedidos.stream()
                    .filter(p -> p.getId() == idReferencia)
                    .findFirst().orElse(null);

            if (pedidoRef == null) {
                System.err.println("❌ Pedido referencia no encontrado: " + idReferencia);
                return false;
            }

            String codigoGrupo = pedidoRef.getCodigo(); // EJ: "ORD-12345"

            // 2. Verificar si ya existe factura para este grupo
            List<Factura> facturas = obtenerTodasLasFacturas();
            if (facturas.stream().anyMatch(f -> f.getCodigoPedido() != null && f.getCodigoPedido().equals(codigoGrupo))) {
                System.err.println("⚠️ Ya existe factura para el código " + codigoGrupo);
                return false; 
            }

            // 3. Agrupar: Sumar todos los items con ese código
            List<Pedido> itemsDelGrupo = pedidos.stream()
                    .filter(p -> p.getCodigo() != null && p.getCodigo().equals(codigoGrupo))
                    .collect(Collectors.toList());

            double totalGrupo = itemsDelGrupo.stream().mapToDouble(Pedido::getTotal).sum();
            int itemsCount = itemsDelGrupo.stream().mapToInt(Pedido::getCantidad).sum();

            // 4. Crear la Factura Única
            Factura f = new Factura();
            f.setCodigoPedido(codigoGrupo);
            f.setClienteCorreo(pedidoRef.getUsuario());
            f.setDescripcion("Pedido " + codigoGrupo + " (" + itemsDelGrupo.size() + " items)");
            f.setCantidadItems(itemsCount);
            
            double subtotal = totalGrupo;
            double iva = subtotal * IVA_TASA;
            double total = subtotal + iva;

            f.setSubtotal(subtotal);
            f.setIva(iva);
            f.setTotal(total);
            f.setEstado("PENDIENTE");
            f.setFechaEmision(LocalDate.now().toString());

            long nextId = facturas.stream().mapToLong(Factura::getId).max().orElse(0) + 1;
            f.setId(nextId);

            facturas.add(f);
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(RUTA_JSON), facturas);

            System.out.println("✅ Factura creada: ID " + f.getId());
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarFactura(long id) {
        try {
            List<Factura> facturas = obtenerTodasLasFacturas();
            boolean eliminado = facturas.removeIf(f -> f.getId() == id);
            if (eliminado) {
                mapper.writerWithDefaultPrettyPrinter().writeValue(new File(RUTA_JSON), facturas);
                return true;
            }
            return false;
        } catch (Exception e) { return false; }
    }

    public boolean actualizarEstado(long id, String nuevoEstado) {
        try {
            List<Factura> facturas = obtenerTodasLasFacturas();
            for (Factura f : facturas) {
                if (f.getId() == id) {
                    f.setEstado(nuevoEstado);
                    mapper.writerWithDefaultPrettyPrinter().writeValue(new File(RUTA_JSON), facturas);
                    return true;
                }
            }
            return false;
        } catch (Exception e) { return false; }
    }
}