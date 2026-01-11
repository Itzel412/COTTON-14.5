package com.franelas.cotton.reclamos;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reclamos")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowedHeaders = "*",
        exposedHeaders = "*",
        methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS },
        maxAge = 3600
)
public class ReclamoController {

    private final ReclamoService reclamoService;

    public ReclamoController(ReclamoService reclamoService) {
        this.reclamoService = reclamoService;
    }

    @RequestMapping(method = RequestMethod.OPTIONS, path = { "", "/", "/{id}", "/{id}/estado" })
    public ResponseEntity<Void> preflight() {
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Reclamo>> listar(
            @RequestHeader(value = "X-User-Role", required = false) String rol,
            @RequestHeader(value = "X-User-Email", required = false) String email
    ) {
        String rolN = rol != null ? rol.trim().toUpperCase() : "";

        if ("ADMIN".equals(rolN)) {
            return ResponseEntity.ok(reclamoService.obtenerTodos());
        }

        if (email != null && !email.trim().isEmpty()) {
            return ResponseEntity.ok(reclamoService.obtenerPorUsuario(email.trim()));
        }

        return ResponseEntity.ok(reclamoService.obtenerTodos());
    }

    @PostMapping
    public ResponseEntity<Boolean> crear(
            @RequestBody Reclamo nuevo,
            @RequestHeader(value = "X-User-Email", required = false) String email
    ) {
        if (nuevo == null) return ResponseEntity.ok(false);

        if (email != null && !email.trim().isEmpty()) {
            nuevo.setUsuario(email.trim());
        }

        boolean ok = reclamoService.crearReclamo(nuevo);
        return ResponseEntity.ok(ok);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Boolean> actualizarEstado(
            @PathVariable("id") long id,
            @RequestBody String estadoRaw,
            @RequestHeader(value = "X-User-Role", required = false) String rol
    ) {
        try {
            String rolN = rol != null ? rol.trim().toUpperCase() : "";
            if (!"ADMIN".equals(rolN)) return ResponseEntity.ok(false);

            if (estadoRaw == null) return ResponseEntity.ok(false);

            String estadoLimpio = estadoRaw.replace("\"", "").trim();
            boolean ok = reclamoService.actualizarEstado(id, estadoLimpio);
            return ResponseEntity.ok(ok);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> eliminar(
            @PathVariable("id") long id,
            @RequestHeader(value = "X-User-Role", required = false) String rol
    ) {
        try {
            String rolN = rol != null ? rol.trim().toUpperCase() : "";
            if (!"ADMIN".equals(rolN)) return ResponseEntity.ok(false);

            boolean ok = reclamoService.eliminarReclamo(id);
            return ResponseEntity.ok(ok);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
    }
}
