package com.franelas.cotton.reclamos;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reclamos")
@CrossOrigin(origins = "http://localhost:5173") 
public class ReclamoController {

    private final ReclamoService reclamoService;

    public ReclamoController(ReclamoService reclamoService) {
        this.reclamoService = reclamoService;
    }

    @GetMapping
    public ResponseEntity<List<Reclamo>> listar() {
        return ResponseEntity.ok(reclamoService.obtenerTodos());
    }

    @PostMapping
    public ResponseEntity<Boolean> crear(@RequestBody Reclamo nuevo) {
        boolean ok = reclamoService.crearReclamo(nuevo);
        return ResponseEntity.ok(ok);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Boolean> actualizarEstado(@PathVariable long id,
                                                    @RequestBody String estadoRaw) {
        try {
            if (estadoRaw == null) {
                return ResponseEntity.ok(false);
            }
            String estadoLimpio = estadoRaw.replace("\"", "").trim();
            boolean ok = reclamoService.actualizarEstado(id, estadoLimpio);
            return ResponseEntity.ok(ok);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
    }
}
