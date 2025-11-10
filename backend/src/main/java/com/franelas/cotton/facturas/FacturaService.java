package com.franelas.cotton.facturas;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class FacturaService {

    private final String RUTA_JSON = "src/main/resources/data/facturas.json";
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Factura> obtenerFacturas() {
        try {
            File jsonFile = new File(RUTA_JSON);

            if (jsonFile.exists() && jsonFile.length() > 0) {
                return mapper.readValue(
                        jsonFile,
                        new TypeReference<List<Factura>>() {}
                );
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error al leer facturas: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean registrarFactura(Factura nuevaFactura) {
        try {
            File jsonFile = new File(RUTA_JSON);
            List<Factura> facturas = obtenerFacturas(); // reutilizamos el método

            if (nuevaFactura.getId() == null || nuevaFactura.getId().isEmpty()) {

                long nextId = facturas.stream()
                        .map(Factura::getId)             // String
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .mapToLong(s -> {
                            try {
                                return Long.parseLong(s);
                            } catch (NumberFormatException e) {
                                return 0L;
                            }
                        })
                        .max()
                        .orElse(0L) + 1;

                nuevaFactura.setId(String.valueOf(nextId));
            }

            facturas.add(nuevaFactura);
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, facturas);

            System.out.println("Factura registrada correctamente: " + nuevaFactura.getId());
            return true;

        } catch (Exception e) {
            System.err.println("Error al registrar factura: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
