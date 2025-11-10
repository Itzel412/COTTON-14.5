// En: com/franelas/cotton/perfil/PerfilController.java
package com.franelas.cotton.perfil;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final PerfilService perfilService;

    // Inyectamos el servicio
    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    /**
     * HISTORIA DE USUARIO: "Consultar Perfil"
     * URL: GET http://localhost:8080/api/perfil/todos
     */
    @GetMapping("/todos")
    public List<Perfil> consultarTodosLosPerfiles() {
        return perfilService.obtenerTodosLosPerfiles();
    }

    /**
     * HISTORIA DE USUARIO: "Crear Perfil"
     * URL: POST http://localhost:8080/api/perfil/registrar
     */
    @PostMapping("/registrar")
    public boolean registrarPerfil(@RequestBody Perfil nuevoPerfil) {
        // @RequestBody toma el JSON del frontend y lo convierte en un objeto Perfil
        return perfilService.registrarPerfil(nuevoPerfil);
    }
}