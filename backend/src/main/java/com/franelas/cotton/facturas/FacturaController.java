package com.franelas.cotton.facturas;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/facturas")
@CrossOrigin(origins = "http://localhost:5173") 
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }
    
    @GetMapping
    public List<Factura> listarFacturas() {
        return facturaService.obtenerTodasLasFacturas();
    }

    @PostMapping
    public boolean crearFactura(@RequestBody Map<String, Object> payload) {
        System.out.println("📩 Petición recibida en crearFactura: " + payload);
        try {
            Object idObj = payload.get("id");
            if (idObj == null) return false;

            long idPedidoRef = Long.parseLong(idObj.toString());
            
            Factura tempFactura = new Factura();
            tempFactura.setId(idPedidoRef); 

            return facturaService.registrarFactura(tempFactura);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- CORRECCIÓN AQUÍ: Agregamos ("id") explícitamente ---
    @DeleteMapping("/{id}")
    public boolean eliminarFactura(@PathVariable("id") long id) {
        System.out.println("🗑️ Solicitud de eliminar ID: " + id);
        return facturaService.eliminarFactura(id);
    }

    // --- CORRECCIÓN AQUÍ: Agregamos ("id") explícitamente ---
    @PutMapping("/{id}/estado")
    public boolean actualizarEstado(@PathVariable("id") long id, @RequestBody String nuevoEstado) {
        System.out.println("🔄 Solicitud de estado ID: " + id + " -> " + nuevoEstado);
        String estadoLimpio = nuevoEstado.replace("\"", "").trim();
        return facturaService.actualizarEstado(id, estadoLimpio);
    }
}