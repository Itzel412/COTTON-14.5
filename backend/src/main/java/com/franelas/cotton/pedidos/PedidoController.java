package com.franelas.cotton.pedidos;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")

@CrossOrigin(origins = "http://localhost:5173")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public boolean crearPedidos(@RequestBody List<Pedido> nuevosPedidos) {
        return pedidoService.registrarMultiplesPedidos(nuevosPedidos);
    }

    @GetMapping
    public List<Pedido> listarPedidos() {
        return pedidoService.obtenerTodosLosPedidos();
    }
}