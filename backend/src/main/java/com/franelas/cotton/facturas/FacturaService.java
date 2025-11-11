package com.franelas.cotton.facturas;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.franelas.cotton.pedidos.Pedido;
import com.franelas.cotton.pedidos.PedidoService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class FacturaService {

    private static final double IVA_TASA = 0.16; // 16% IVA

    private final String RUTA_JSON = "src/main/resources/data/facturas.json";
    private final ObjectMapper mapper = new ObjectMapper();

    private final PedidoService pedidoService;

    public FacturaService(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    public List<Factura> obtenerTodasLasFacturas() {
        try {
            File jsonFile = new File(RUTA_JSON);
            if (!jsonFile.exists() || jsonFile.length() == 0) {
                System.err.println("No hay facturas registradas aún.");
                return Collections.emptyList();
            }

            return mapper.readValue(jsonFile, new TypeReference<List<Factura>>() {});
        } catch (Exception e) {
            System.err.println("Error al leer el archivo de facturas: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public boolean registrarFactura(Factura nuevaFactura) {
        try {
            if (nuevaFactura == null) {
                System.err.println("La factura no puede ser nula.");
                return false;
            }

            long idPedido = nuevaFactura.getIdPedido();
            if (idPedido <= 0) {
                System.err.println("idPedido inválido para facturar.");
                return false;
            }

            List<Pedido> pedidos = pedidoService.obtenerTodosLosPedidos();
            Pedido pedido = pedidos.stream()
                    .filter(p -> p.getId() == idPedido)
                    .findFirst()
                    .orElse(null);

            if (pedido == null) {
                System.err.println("No existe pedido con id " + idPedido);
                return false;
            }

            File jsonFile = new File(RUTA_JSON);
            List<Factura> facturas;
            if (jsonFile.exists() && jsonFile.length() > 0) {
                facturas = mapper.readValue(jsonFile, new TypeReference<List<Factura>>() {});
            } else {
                facturas = new ArrayList<>();
            }

            boolean yaFacturada = facturas.stream()
                    .anyMatch(f -> f.getIdPedido() == idPedido);

            if (yaFacturada) {
                System.err.println("El pedido " + idPedido + " ya tiene una factura asociada.");
                return false;
            }

            Factura factura = new Factura();
            factura.setIdPedido(idPedido);
            factura.setClienteCorreo(pedido.getUsuario());
            factura.setColor(pedido.getColor());
            factura.setTalla(pedido.getTalla());
            factura.setCantidad(pedido.getCantidad());
            factura.setPrecioUnitario(pedido.getPrecioUnitario());

            double subtotal = pedido.getCantidad() * pedido.getPrecioUnitario();
            double iva = subtotal * IVA_TASA;
            double total = subtotal + iva;

            factura.setSubtotal(subtotal);
            factura.setIva(iva);
            factura.setTotal(total);

            String fechaEmision = nuevaFactura.getFechaEmision();
            if (fechaEmision == null || fechaEmision.isBlank()) {
                fechaEmision = LocalDate.now().toString();
            }
            factura.setFechaEmision(fechaEmision);

            long nextId = facturas.stream()
                    .mapToLong(Factura::getId)
                    .max()
                    .orElse(0) + 1;
            factura.setId(nextId);

            facturas.add(factura);
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, facturas);

            System.out.println("Factura registrada exitosamente con ID: " + factura.getId());
            return true;

        } catch (Exception e) {
            System.err.println("Error al registrar factura: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
