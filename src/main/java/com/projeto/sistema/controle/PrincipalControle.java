package com.projeto.sistema.controle;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.projeto.sistema.modelos.UF;
import com.projeto.sistema.modelos.Usuario;
import com.projeto.sistema.repositorios.ContatosRepositorio;
import com.projeto.sistema.repositorios.GrupoRepositorio;
import com.projeto.sistema.repositorios.UsuarioRepositorio;

@Controller
public class PrincipalControle {
	
    @Autowired private ContatosRepositorio contatosRepositorio;
    @Autowired private GrupoRepositorio grupoRepositorio;
    @Autowired private UsuarioRepositorio usuarioRepositorio;
    @Autowired private PasswordEncoder passwordEncoder;

    // --- DASHBOARD (Home) ---
    @GetMapping("/administrativo")
    public ModelAndView acessarPrincipal() {
        ModelAndView mv = new ModelAndView("administrativo/home");
        
        // Dados para os cards e tabelas
        mv.addObject("totalContatos", contatosRepositorio.count()); 
        mv.addObject("ultimosContatos", contatosRepositorio.findTop10ByOrderByIdDesc());
        mv.addObject("listaEstados", UF.values());
        
        // Lista de grupos para o modal de cadastro funcionar
        mv.addObject("listaGrupos", grupoRepositorio.findAll()); 
        
        return mv;
    }
    
    // --- PERFIL (Exibir) ---
    @GetMapping("/perfil")
    public ModelAndView perfil(Principal principal) {
        ModelAndView mv = new ModelAndView("administrativo/perfil");
        
        // Busca o usuário logado no banco de dados
        Usuario usuario = usuarioRepositorio.findByUsername(principal.getName());
        
        mv.addObject("usuario", usuario);
        return mv;
    }

    // --- PERFIL (Salvar Dados) ---
    @PostMapping("/perfil/salvar")
    public String salvarPerfil(Usuario usuarioForm, Principal principal, RedirectAttributes attributes) {
        Usuario usuarioBanco = usuarioRepositorio.findByUsername(principal.getName());
        
        usuarioBanco.setNome(usuarioForm.getNome());
        usuarioBanco.setEmail(usuarioForm.getEmail());
        
        usuarioRepositorio.save(usuarioBanco);
        
        attributes.addFlashAttribute("mensagem", "Dados atualizados com sucesso!");
        attributes.addFlashAttribute("tipoMensagem", "success");
        
        return "redirect:/perfil";
    }

    // --- PERFIL (Alterar Senha) ---
    @PostMapping("/perfil/senha")
    public String alterarSenha(@RequestParam("senhaAtual") String senhaAtual,
                               @RequestParam("novaSenha") String novaSenha,
                               Principal principal, 
                               RedirectAttributes attributes) {
        
        Usuario usuario = usuarioRepositorio.findByUsername(principal.getName());
        
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            attributes.addFlashAttribute("mensagem", "Senha atual incorreta!");
            attributes.addFlashAttribute("tipoMensagem", "danger");
            return "redirect:/perfil";
        }
        
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepositorio.save(usuario);
        
        attributes.addFlashAttribute("mensagem", "Senha alterada com sucesso!");
        attributes.addFlashAttribute("tipoMensagem", "success");
        
        return "redirect:/perfil";
    }
}