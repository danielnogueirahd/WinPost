package com.projeto.sistema.controle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.projeto.sistema.servicos.EmpresaService;

@Controller
@RequestMapping("/administrativo/empresas")
public class EmpresaControle {

    @Autowired
    private EmpresaService empresaService;

    // Apenas quem tem permissão global pode aceder a esta tela
    @GetMapping
    @PreAuthorize("hasAuthority('CONFIGURACOES_SISTEMA')") 
    public ModelAndView listar() {
        ModelAndView mv = new ModelAndView("administrativo/empresas/lista");
        mv.addObject("listaEmpresas", empresaService.listarTodas());
        mv.addObject("paginaAtiva", "empresas");
        return mv;
    }
    
    // Aqui você criará os métodos @PostMapping para salvar e @GetMapping para a tela de cadastro
}