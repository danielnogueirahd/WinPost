package com.projeto.sistema.controle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.projeto.sistema.modelos.UF;
import com.projeto.sistema.repositorios.ContatosRepositorio;
import com.projeto.sistema.repositorios.GrupoRepositorio; // <--- Import Novo

@Controller
public class PrincipalControle {
	
    @Autowired
    private ContatosRepositorio contatosRepositorio;

    @Autowired
    private GrupoRepositorio grupoRepositorio; // <--- Injeção Nova

    @GetMapping("/administrativo")
    public ModelAndView acessarPrincipal() {
        ModelAndView mv = new ModelAndView("administrativo/home");
        
        mv.addObject("totalContatos", contatosRepositorio.count()); 
        mv.addObject("ultimosContatos", contatosRepositorio.findTop10ByOrderByIdDesc());
        mv.addObject("listaEstados", UF.values());
        
        // --- NOVO: Envia os grupos para o modal de relatório funcionar na Home ---
        mv.addObject("listaGrupos", grupoRepositorio.findAll()); 
        // ------------------------------------------------------------------------
        
        return mv;
    }
    
    @GetMapping("/perfil")
    public ModelAndView perfil() {
        ModelAndView mv = new ModelAndView("administrativo/perfil");
        
        mv.addObject("nomeUsuario", "Admin do Sistema");
        mv.addObject("emailUsuario", "admin@input.tecnologia.com");
        mv.addObject("funcaoUsuario", "Administrador Master");
        
        return mv;
    }
}