package com.franelas.cotton.facturas;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facturas")
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
    public boolean crearFactura(@RequestBody Factura factura) {
        return facturaService.registrarFactura(factura);
    }
}
