package com.franelas.cotton.facturas;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

@Service
public class FacturaService {

    private static final double IVA_TASA = 0.16;

    private static final String RUTA_FOLDER = "src/main/resources/data";
    private static final String RUTA_JSON = RUTA_FOLDER + "/facturas.json";

    private static final Set<String> ESTADOS_VALIDOS =
            new HashSet<>(Arrays.asList("PENDIENTE", "PAGADA", "ANULADA"));

    private final ObjectMapper mapper = new ObjectMapper();

    public FacturaService() {
        crearArchivoSiNoExiste();
    }

    private void crearArchivoSiNoExiste() {
        try {
            File folder = new File(RUTA_FOLDER);
            if (!folder.exists()) folder.mkdirs();

            File file = new File(RUTA_JSON);
            if (!file.exists()) {
                mapper.writerWithDefaultPrettyPrinter().writeValue(file, new ArrayList<>());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Factura> obtenerTodasLasFacturas() {
        try {
            File jsonFile = new File(RUTA_JSON);
            if (!jsonFile.exists() || jsonFile.length() == 0) return new ArrayList<>();
            return mapper.readValue(jsonFile, new TypeReference<List<Factura>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public boolean existeFacturaParaCodigoPedido(String codigoPedido) {
        if (codigoPedido == null || codigoPedido.trim().isEmpty()) return false;
        List<Factura> facturas = obtenerTodasLasFacturas();
        return facturas.stream().anyMatch(f ->
                f.getCodigoPedido() != null && f.getCodigoPedido().equalsIgnoreCase(codigoPedido.trim())
        );
    }

    public boolean registrarFacturaAgrupada(String codigoPedido, String clienteCorreo, int cantidadItems, double subtotalBase) {
        try {
            if (codigoPedido == null || codigoPedido.trim().isEmpty()) return false;
            if (clienteCorreo == null || clienteCorreo.trim().isEmpty()) return false;
            if (cantidadItems <= 0) return false;
            if (subtotalBase <= 0) return false;

            List<Factura> facturas = obtenerTodasLasFacturas();

            if (facturas.stream().anyMatch(f ->
                    f.getCodigoPedido() != null && f.getCodigoPedido().equalsIgnoreCase(codigoPedido.trim())
            )) {
                System.err.println("Ya existe factura para el pedido " + codigoPedido);
                return false;
            }

            Factura f = new Factura();
            long nextId = facturas.stream().mapToLong(Factura::getId).max().orElse(0) + 1;
            f.setId(nextId);

            f.setCodigoPedido(codigoPedido.trim());
            f.setClienteCorreo(clienteCorreo.trim());

            f.setDescripcion("Pedido " + codigoPedido.trim() + " (" + cantidadItems + " unidades)");
            f.setCantidadItems(cantidadItems);

            double subtotal = subtotalBase;
            double iva = subtotal * IVA_TASA;
            double total = subtotal + iva;

            f.setSubtotal(subtotal);
            f.setIva(iva);
            f.setTotal(total);

            f.setEstado("PENDIENTE");
            f.setFechaEmision(LocalDate.now().toString());

            facturas.add(f);

            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(RUTA_JSON), facturas);
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
            if (!eliminado) return false;

            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(RUTA_JSON), facturas);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean actualizarEstado(long id, String nuevoEstado) {
        try {
            if (nuevoEstado == null) return false;
            String estado = nuevoEstado.replace("\"", "").trim().toUpperCase();

            if (!ESTADOS_VALIDOS.contains(estado)) {
                System.err.println("Estado inválido: " + estado);
                return false;
            }

            List<Factura> facturas = obtenerTodasLasFacturas();
            for (Factura f : facturas) {
                if (f.getId() == id) {
                    f.setEstado(estado);
                    mapper.writerWithDefaultPrettyPrinter().writeValue(new File(RUTA_JSON), facturas);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
