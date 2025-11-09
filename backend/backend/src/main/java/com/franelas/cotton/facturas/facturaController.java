package com.franelas.cotton.facturas;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/facturas")
public class facturaController {

    private final facturaService facturaService;

    public facturaController(facturaService facturaService) {
        this.facturaService = facturaService;
    }

    /**
     * HISTORIA DE USUARIO: "Crear Factura"
     * POST http://localhost:8081/api/facturas
     */
    @PostMapping
    public boolean crearFactura(@RequestBody factura nuevaFactura) {
        return facturaService.registrarFactura(nuevaFactura);
    }

    /**
     * HISTORIA DE USUARIO: "Listar Facturas"
     * GET http://localhost:8081/api/facturas
     */
    @GetMapping
    public List<factura> listarFacturas() {
        return facturaService.obtenerFacturas();
    }
}
//.