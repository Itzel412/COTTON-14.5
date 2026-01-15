package com.franelas.cotton.perfil;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/perfil")
@CrossOrigin(origins = "http://localhost:5173")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    private String normRol(String rolHeader) {
        return rolHeader != null ? rolHeader.trim().toUpperCase() : "";
    }

    private String normEmail(String emailHeader) {
        return emailHeader != null ? emailHeader.trim().toLowerCase() : "";
    }

    @GetMapping("/todos")
    public ResponseEntity<?> consultarTodosLosPerfiles(
            @RequestHeader(value = "X-User-Role", required = false) String rolHeader
    ) {
        String rolN = normRol(rolHeader);
        if (!"ADMIN".equals(rolN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No autorizado.");
        }
        return ResponseEntity.ok(perfilService.obtenerTodosLosPerfiles());
    }

    @GetMapping("/mi")
    public ResponseEntity<?> miPerfil(
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader
    ) {
        String emailN = normEmail(emailHeader);
        if (emailN.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Falta X-User-Email.");
        }

        Perfil p = perfilService.buscarPorCorreo(emailN);
        if (p == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Perfil no encontrado.");
        }
        p.setClave(null);
        return ResponseEntity.ok(p);
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarPerfil(
            @RequestBody Perfil nuevoPerfil,
            @RequestHeader(value = "X-User-Role", required = false) String rolHeader
    ) {
        String rolN = normRol(rolHeader);

        if (!"ADMIN".equals(rolN)) {
            nuevoPerfil.setRol("CLIENTE");
        }

        String error = perfilService.registrarPerfil(nuevoPerfil);
        if (error != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        Perfil creado = perfilService.buscarPorCorreo(nuevoPerfil.getCorreo());
        if (creado != null) creado.setClave(null);

        return ResponseEntity.ok(creado != null ? creado : true);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Perfil perfil = perfilService.buscarPorCorreoYClave(
                request.getCorreo(),
                request.getClave()
        );

        if (perfil == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Correo o clave incorrectos");
        }

        perfil.setClave(null);
        return ResponseEntity.ok(perfil);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarPerfil(
            @PathVariable("id") long id,
            @RequestBody Perfil perfilEditado,
            @RequestHeader(value = "X-User-Role", required = false) String rolHeader,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader
    ) {
        String rolN = normRol(rolHeader);
        String emailN = normEmail(emailHeader);

        Perfil original = perfilService.buscarPorId(id);
        if (original == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Perfil no encontrado para editar.");
        }

        if (!"ADMIN".equals(rolN)) {
            if (emailN.isEmpty() || !original.getCorreo().trim().toLowerCase().equals(emailN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No autorizado para editar este perfil.");
            }
            perfilEditado.setRol(original.getRol());
        }

        perfilEditado.setId(id);

        String error = perfilService.actualizarPerfil(original, perfilEditado);
        if (error != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        return ResponseEntity.ok(true);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPerfil(
            @PathVariable("id") long id,
            @RequestHeader(value = "X-User-Role", required = false) String rolHeader,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader
    ) {
        String rolN = normRol(rolHeader);
        String emailN = normEmail(emailHeader);

        Perfil original = perfilService.buscarPorId(id);
        if (original == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Perfil no encontrado para eliminar.");
        }

        if (!"ADMIN".equals(rolN)) {
            if (emailN.isEmpty() || !original.getCorreo().trim().toLowerCase().equals(emailN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No autorizado para eliminar este perfil.");
            }
        }

        String error = perfilService.eliminarPerfil(id);
        if (error != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        return ResponseEntity.ok(true);
    }
}
