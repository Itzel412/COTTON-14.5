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

    // Validar reglas del ERS
    public boolean validarPedido(Pedido pedido) {
        if (pedido.getCantidad() > 10) {
            System.err.println("Error: no se pueden pedir más de 10 unidades del mismo producto.");
            return false;
        }
        if (pedido.getTalla() == null || pedido.getColor() == null) {
            System.err.println("Error: talla y color son obligatorios.");
            return false;
        }
        return true;
    }

    //  Guardar un nuevo pedido
    public boolean registrarPedido(Pedido nuevoPedido) {
        try {
            File jsonFile = new File(RUTA_JSON);
            List<Pedido> pedidos;

            if (jsonFile.exists() && jsonFile.length() > 0) {
                pedidos = mapper.readValue(jsonFile, new TypeReference<List<Pedido>>() {});
            } else {
                pedidos = new ArrayList<>();
            }

            pedidos.add(nuevoPedido);
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, pedidos);

            System.out.println("Pedido guardado correctamente con ID: " + nuevoPedido.getId());
            return true;
        } catch (Exception e) {
            System.err.println("Error al guardar pedido: " + e.getMessage());
            return false;
        }
    }

    // Consultar todos los pedidos
    public List<Pedido> obtenerPedidos() {
        try {
            File jsonFile = new File(RUTA_JSON);
            if (!jsonFile.exists() || jsonFile.length() == 0) {
                return Collections.emptyList();
            }
            return mapper.readValue(jsonFile, new TypeReference<List<Pedido>>() {});
        } catch (Exception e) {
            System.err.println("Error al leer pedidos: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
