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

    @GetMapping("/todos")
    public List<Perfil> consultarTodosLosPerfiles() {
        return perfilService.obtenerTodosLosPerfiles();
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarPerfil(@RequestBody Perfil nuevoPerfil) {
        String error = perfilService.registrarPerfil(nuevoPerfil);

        if (error != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        return ResponseEntity.ok(true);
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
}
