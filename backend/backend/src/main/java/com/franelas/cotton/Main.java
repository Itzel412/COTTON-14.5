package com.franelas.cotton;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.Collections; // <-- (1. Importa esto)

@SpringBootApplication
public class Main {

    public static void main(String[] args) {

        // (2. En lugar de una línea, lo hacemos en tres)
        SpringApplication app = new SpringApplication(Main.class);

        // (3. Aquí le decimos que use el puerto 8081)
        app.setDefaultProperties(Collections.singletonMap("server.port", "8081"));

        // (4. Y ahora la ejecutamos)
        app.run(args);
    }
}