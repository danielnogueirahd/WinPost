package com.projeto.sistema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer; // Import necessário
import org.springframework.boot.builder.SpringApplicationBuilder; // Import necessário
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SistemaApplication extends SpringBootServletInitializer { // 1. Herdar daqui

    // 2. Sobrescrever este método para o Tomcat saber como iniciar
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SistemaApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(SistemaApplication.class, args);
    }
}