package com.projeto.sistema;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/css/**", "/js/**", "/img/**").permitAll() 
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                // AQUI ESTÁ A CORREÇÃO: O caminho exato da sua home!
                .defaultSuccessUrl("/administrativo/agenda", true) 
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout") 
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true) 
                .clearAuthentication(true) 
                .permitAll()
            );
            
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}