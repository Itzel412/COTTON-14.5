package com.franelas.cotton.reclamos;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reclamos")
public class reclamoController {

    private final reclamoService reclamoService;

    public reclamoController(reclamoService reclamoService) {
        this.reclamoService = reclamoService;
    }

    /**
     * HISTORIA DE USUARIO: "Crear Reclamo"
     * POST /api/reclamos
     */
    @PostMapping
    public boolean crearReclamo(@RequestBody reclamo nuevoReclamo) {
        return reclamoService.registrarReclamo(nuevoReclamo);
    }

    /**
     * HISTORIA DE USUARIO: "Listar Reclamos"
     * GET /api/reclamos
     */
    @GetMapping
    public List<reclamo> listarReclamos() {
        return reclamoService.obtenerReclamos();
    }
}
//.