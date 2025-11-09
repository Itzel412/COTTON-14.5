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
     * HISTORIA DE USUARIO: "Realizar Pedido"
     * URL: POST http://localhost:8080/api/pedidos
     */
    @PostMapping
    public boolean crearPedido(@RequestBody Pedido nuevoPedido) {
        if (!pedidoService.validarPedido(nuevoPedido)) {
            return false;
        }
        return pedidoService.registrarPedido(nuevoPedido);
    }

    /**
     * HISTORIA DE USUARIO: "Ver Pedido"
     * URL: GET http://localhost:8080/api/pedidos
     */
    @GetMapping
    public List<Pedido> listarPedidos() {
        return pedidoService.obtenerPedidos();
    }
}
