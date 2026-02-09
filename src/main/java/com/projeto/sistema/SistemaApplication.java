package com.projeto.sistema;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer; // Import necessário
import org.springframework.boot.builder.SpringApplicationBuilder; // Import necessário
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.projeto.sistema.modelos.Usuario;
import com.projeto.sistema.repositorios.UsuarioRepositorio;

@SpringBootApplication
public class SistemaApplication extends SpringBootServletInitializer { // 1. Estender a classe

    // 2. Sobrescrever o método configure
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SistemaApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(SistemaApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(UsuarioRepositorio repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.findByUsername("admin") == null) {
                Usuario u = new Usuario();
                u.setNome("Administrador Master");
                u.setEmail("admin@winpost.com");
                u.setUsername("admin");
                u.setSenha(encoder.encode("123456"));
                u.setCargo("Administrador");
                
                repo.save(u);
                System.out.println(">>> USUÁRIO ADMIN CRIADO COM SUCESSO! <<<");
            }
        };
    }
}