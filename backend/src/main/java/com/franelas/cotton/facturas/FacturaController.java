package com.franelas.cotton.facturas;

import com.franelas.cotton.pedidos.Pedido;
import com.franelas.cotton.pedidos.PedidoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/facturas")
@CrossOrigin(origins = "http://localhost:5173")
public class FacturaController {

    private final FacturaService facturaService;
    private final PedidoService pedidoService;

    public FacturaController(FacturaService facturaService, PedidoService pedidoService) {
        this.facturaService = facturaService;
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public List<Factura> listarFacturas() {
        return facturaService.obtenerTodasLasFacturas();
    }

    @PostMapping
    public boolean crearFactura(@RequestBody Map<String, Object> payload) {
        try {
            Object idObj = payload.get("id");
            if (idObj == null) return false;

            long idPedidoRef = Long.parseLong(idObj.toString());

            List<Pedido> pedidos = pedidoService.obtenerTodosLosPedidos();

            Pedido pedidoRef = pedidos.stream()
                    .filter(p -> p.getId() == idPedidoRef)
                    .findFirst()
                    .orElse(null);

            if (pedidoRef == null) {
                System.err.println("Pedido referencia no encontrado: " + idPedidoRef);
                return false;
            }

            String codigoGrupo = pedidoRef.getCodigo();
            if (codigoGrupo == null || codigoGrupo.trim().isEmpty()) return false;

            if (facturaService.existeFacturaParaCodigoPedido(codigoGrupo)) {
                System.err.println(" Ya existe factura para " + codigoGrupo);
                return false;
            }

            List<Pedido> itemsDelGrupo = pedidos.stream()
                    .filter(p -> p.getCodigo() != null && p.getCodigo().equalsIgnoreCase(codigoGrupo))
                    .collect(Collectors.toList());

            if (itemsDelGrupo.isEmpty()) return false;

            double subtotalBase = itemsDelGrupo.stream().mapToDouble(Pedido::getTotal).sum();
            int cantidadItems = itemsDelGrupo.stream().mapToInt(Pedido::getCantidad).sum();

            return facturaService.registrarFacturaAgrupada(
                    codigoGrupo,
                    pedidoRef.getUsuario(),
                    cantidadItems,
                    subtotalBase
            );

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @DeleteMapping("/{id}")
    public boolean eliminarFactura(@PathVariable("id") long id) {
        return facturaService.eliminarFactura(id);
    }

    @PutMapping("/{id}/estado")
    public boolean actualizarEstado(@PathVariable("id") long id, @RequestBody String nuevoEstado) {
        return facturaService.actualizarEstado(id, nuevoEstado);
    }
}
