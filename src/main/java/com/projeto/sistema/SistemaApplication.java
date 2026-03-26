package com.projeto.sistema;

import java.util.Arrays; 
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.projeto.sistema.modelos.Usuario;
import com.projeto.sistema.modelos.Perfil;
import com.projeto.sistema.modelos.Permissao;
import com.projeto.sistema.repositorios.UsuarioRepositorio;
import com.projeto.sistema.repositorios.PerfilRepositorio; 

@SpringBootApplication
@EnableJpaAuditing 
public class SistemaApplication extends SpringBootServletInitializer { 

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SistemaApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(SistemaApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(UsuarioRepositorio repo, PerfilRepositorio perfilRepo, PasswordEncoder encoder) {
        return args -> {
            
            // 1. Garante que o perfil MASTER existe
            Perfil perfilMaster = perfilRepo.findByNome("MASTER");
            if (perfilMaster == null) {
                perfilMaster = new Perfil();
                perfilMaster.setNome("MASTER");
                perfilMaster.setPermissoes(Arrays.asList(Permissao.values())); 
                perfilRepo.save(perfilMaster);
            }

            // 2. Busca o admin no banco
            Usuario admin = repo.findByUsername("admin");
            if (admin == null) {
                Usuario u = new Usuario();
                u.setNome("Administrador Master");
                u.setEmail("admin@winpost.com");
                u.setUsername("admin");
                u.setSenha(encoder.encode("123456"));
                u.setPerfil(perfilMaster); 
                repo.save(u);
            } else if (admin.getPerfil() == null) {
                admin.setPerfil(perfilMaster);
                repo.save(admin);
            }

            // ==========================================
            // CRIANDO O NOSSO USUÁRIO DE TESTE (Cobaia)
            // ==========================================

            // 3. Cria um Perfil VENDEDOR
            Perfil perfilVendedor = perfilRepo.findByNome("VENDEDOR");
            if (perfilVendedor == null) {
                perfilVendedor = new Perfil();
                perfilVendedor.setNome("VENDEDOR");
                // Olha a tesoura aqui: Ele SÓ recebe a permissão de VISUALIZAR
                perfilVendedor.setPermissoes(Arrays.asList(Permissao.CONTATO_VISUALIZAR)); 
                perfilRepo.save(perfilVendedor);
            }

            // 4. Cria o usuário vendedor e vincula ao perfil fraco
            Usuario vendedor = repo.findByUsername("vendedor");
            if (vendedor == null) {
                Usuario v = new Usuario();
                v.setNome("Vendedor Teste");
                v.setEmail("vendedor@winpost.com");
                v.setUsername("vendedor");
                v.setSenha(encoder.encode("123456")); // Mesma senha para facilitar
                v.setPerfil(perfilVendedor);
                repo.save(v);
                System.out.println(">>> USUÁRIO VENDEDOR DE TESTE CRIADO COM SUCESSO! <<<");
            }
        };
    }
}