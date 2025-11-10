package com.franelas.cotton.facturas;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facturas")
@CrossOrigin(origins = "http://localhost:5173")
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @PostMapping("/crear")
    public boolean crearFactura(@RequestBody Factura nuevaFactura) {
        return facturaService.registrarFactura(nuevaFactura);
    }

    @GetMapping("/todas")
    public List<Factura> listarFacturas() {
        return facturaService.obtenerFacturas();
    }
}
