package com.franelas.cotton.pedidos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PedidoService {

    private final String RUTA_JSON = "backend/backend/src/main/resources/data/pedidos.json";
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Obtener todos los pedidos guardados
     */
    public List<Pedido> obtenerTodosLosPedidos() {
        try {
            File jsonFile = new File(RUTA_JSON);
            if (!jsonFile.exists() || jsonFile.length() == 0) {
                System.err.println("No hay pedidos registrados aún.");
                return Collections.emptyList();
            }
            return mapper.readValue(jsonFile, new TypeReference<List<Pedido>>() {});
        } catch (Exception e) {
            System.err.println("Error al leer el archivo de pedidos: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Registrar un pedido nuevo con ID autogenerado
     */
    public boolean registrarPedido(Pedido nuevoPedido) {
        try {
            File jsonFile = new File(RUTA_JSON);
            List<Pedido> pedidos;

            // Leer pedidos existentes
            if (jsonFile.exists() && jsonFile.length() > 0) {
                pedidos = mapper.readValue(jsonFile, new TypeReference<List<Pedido>>() {});
            } else {
                pedidos = new ArrayList<>();
            }

            //  Generar ID
            if (nuevoPedido.getId() == 0) {
                long nextId = pedidos.stream()
                        .mapToLong(Pedido::getId)
                        .max()
                        .orElse(0) + 1;
                nuevoPedido.setId(nextId);
            }

            // Calcular total automáticamente
            nuevoPedido.setTotal(nuevoPedido.getCantidad() * nuevoPedido.getPrecioUnitario());

            pedidos.add(nuevoPedido);
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, pedidos);

            System.out.println("Pedido registrado exitosamente con ID: " + nuevoPedido.getId());
            return true;

        } catch (Exception e) {
            System.err.println("Error al registrar pedido: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
