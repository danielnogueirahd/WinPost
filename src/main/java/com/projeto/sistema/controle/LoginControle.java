package com.projeto.sistema.controle;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginControle {
    
    // ESTA É A ROTA QUE FALTA PARA A LANDING PAGE
    @GetMapping("/")
    public String landingPage() {
        return "landing"; // Retorna o ficheiro landing.html
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // Retorna o ficheiro login.html
    }
}