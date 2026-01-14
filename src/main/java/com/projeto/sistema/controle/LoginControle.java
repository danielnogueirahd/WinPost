package com.projeto.sistema.controle;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginControle {
    @GetMapping("/login")
    public String login() {
        return "login"; // Deve retornar o nome do arquivo HTML (login.html)
    }
}