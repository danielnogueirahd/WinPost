package com.projeto.sistema;

import java.util.Arrays; // Import necessário para pegar a lista de permissões
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
import com.projeto.sistema.repositorios.PerfilRepositorio; // Repositório novo importado

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
            
            // 1. Criar o perfil MASTER se ele ainda não existir no banco
            Perfil perfilMaster = perfilRepo.findByNome("MASTER");
            if (perfilMaster == null) {
                perfilMaster = new Perfil();
                perfilMaster.setNome("MASTER");
                // Pega todas as permissões que você cadastrou no Enum e joga no perfil
                perfilMaster.setPermissoes(Arrays.asList(Permissao.values())); 
                perfilRepo.save(perfilMaster);
                System.out.println(">>> PERFIL MASTER CRIADO COM SUCESSO! <<<");
            }

            // 2. Criar o usuário Admin e vincular ao novo perfil MASTER
            if (repo.findByUsername("admin") == null) {
                Usuario u = new Usuario();
                u.setNome("Administrador Master");
                u.setEmail("admin@winpost.com");
                u.setUsername("admin");
                u.setSenha(encoder.encode("123456"));
                
                // AQUI ESTÁ A CORREÇÃO: Sai o setCargo, entra o setPerfil
                u.setPerfil(perfilMaster); 
                
                repo.save(u);
                System.out.println(">>> USUÁRIO ADMIN CRIADO COM SUCESSO! <<<");
            }
        };
    }
}