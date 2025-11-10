package com.franelas.cotton.reclamos;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reclamos")
public class ReclamoController {

    private final ReclamoService ReclamoService;

    public ReclamoController(ReclamoService reclamoService) {
        this.ReclamoService = reclamoService;
    }

    @PostMapping
    public boolean crearReclamo(@RequestBody Reclamo nuevoReclamo) {
        return ReclamoService.registrarReclamo(nuevoReclamo);
    }

    @GetMapping
    public List<Reclamo> listarReclamos() {
        return ReclamoService.obtenerReclamos();
    }
}