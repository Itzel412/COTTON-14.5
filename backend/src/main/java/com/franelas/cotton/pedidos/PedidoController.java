package com.franelas.cotton.pedidos;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /**
     * Crear pedido
     * URL: POST http://localhost:8080/api/pedidos
     */
    @PostMapping
    public boolean crearPedido(@RequestBody Pedido nuevoPedido) {
        return pedidoService.registrarPedido(nuevoPedido);
    }

    /**
     * Consultar todos los pedidos
     * URL: GET http://localhost:8080/api/pedidos
     */
    @GetMapping
    public List<Pedido> listarPedidos() {
        return pedidoService.obtenerTodosLosPedidos();
    }
}
