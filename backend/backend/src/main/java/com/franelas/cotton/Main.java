//itzel puso esto de ejemplo

package com.franelas.cotton;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // <-- ¡La anotación más importante!
public class Main {

    public static void main(String[] args) {
        // Esto arranca todo el servidor web de Spring
        SpringApplication.run(Main.class, args);
    }

    
}