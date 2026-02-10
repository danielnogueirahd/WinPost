package com.projeto.sistema;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	// EM: src/main/java/com/projeto/sistema/SecurityConfig.java

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers("/css/**", "/js/**", "/img/**", "/uploads/**", "/login").permitAll()
	            .requestMatchers("/api/**").authenticated() // Mantenha suas regras
	            .anyRequest().authenticated()
	        )
	        .formLogin(login -> login
	            .loginPage("/login")
	            .defaultSuccessUrl("/administrativo/agenda", true)
	            .permitAll()
	        )
	        .logout(logout -> logout
	            .logoutSuccessUrl("/login?logout")
	            .permitAll()
	        )
	        // --- CORREÇÃO AQUI ---
	        // Opção A: Desativar para tudo (Mais fácil para resolver agora)
	        .csrf(csrf -> csrf.disable()); 
	        
	        // OU Opção B: Adicionar suas rotas de POST na lista de exceções
	        /*
	        .csrf(csrf -> csrf
	            .ignoringRequestMatchers("/api/**", "/grupos/**", "/mensagens/**")
	        );
	        */

	    return http.build();
	}
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}