package com.projeto.sistema.controle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.projeto.sistema.modelos.UF;
import com.projeto.sistema.repositorios.ContatosRepositorio;

@Controller
public class PrincipalControle {
	
    @Autowired
    private ContatosRepositorio contatosRepositorio;
    @GetMapping("/administrativo")
    public ModelAndView acessarPrincipal() {
        ModelAndView mv = new ModelAndView("administrativo/home");
        
        mv.addObject("totalContatos", contatosRepositorio.count()); 
        mv.addObject("ultimosContatos", contatosRepositorio.findTop10ByOrderByIdDesc());
        mv.addObject("listaEstados", UF.values()); 
        
        return mv;
    }
    
    @GetMapping("/perfil")
    public ModelAndView perfil() {
        ModelAndView mv = new ModelAndView("administrativo/perfil");
        
        // Simulando dados do usuário logado (pois ainda não temos tabela de Usuários)
        mv.addObject("nomeUsuario", "Admin do Sistema");
        mv.addObject("emailUsuario", "admin@input.tecnologia.com");
        mv.addObject("funcaoUsuario", "Administrador Master");
        
        return mv;
    }
    }