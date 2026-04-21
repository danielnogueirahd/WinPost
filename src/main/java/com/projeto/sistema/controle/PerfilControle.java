package com.projeto.sistema.controle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.projeto.sistema.modelos.Usuario;
import com.projeto.sistema.modelos.UsuarioLogado;
import com.projeto.sistema.repositorios.UsuarioRepositorio;

@Controller
public class PerfilControle {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // =======================================================================
    // 1. VISUALIZAÇÃO DO PERFIL (GET)
    // =======================================================================
    @GetMapping("/perfil")
    public ModelAndView exibirPerfil(@AuthenticationPrincipal UsuarioLogado usuarioLogado) {
        ModelAndView mv = new ModelAndView("administrativo/perfil");
        
        // CORRIGIDO: getIdUsuario() em vez de getId()
        Usuario usuario = usuarioRepositorio.findById(usuarioLogado.getIdUsuario()).orElse(null);
        mv.addObject("usuario", usuario);
        
        return mv;
    }

    // =======================================================================
    // 2. ATUALIZAR DADOS PESSOAIS (POST)
    // =======================================================================
    @PostMapping("/perfil/salvar")
    public String salvarPerfil(@RequestParam("nome") String nome, 
                               @RequestParam("email") String email,
                               @AuthenticationPrincipal UsuarioLogado usuarioLogado, 
                               RedirectAttributes attributes) {
        
        // CORRIGIDO: getIdUsuario() em vez de getId()
        Usuario usuario = usuarioRepositorio.findById(usuarioLogado.getIdUsuario()).orElse(null);
        
        if (usuario != null) {
            usuario.setNome(nome);
            usuario.setEmail(email);
            usuarioRepositorio.save(usuario);
            
            attributes.addFlashAttribute("mensagem", "Dados pessoais atualizados com sucesso!");
            attributes.addFlashAttribute("tipoMensagem", "success");
        } else {
            attributes.addFlashAttribute("mensagem", "Erro ao encontrar o usuário no sistema.");
            attributes.addFlashAttribute("tipoMensagem", "danger");
        }
        
        return "redirect:/perfil";
    }

    // =======================================================================
    // 3. SEGURANÇA: ALTERAÇÃO DE SENHA (POST)
    // =======================================================================
    @PostMapping("/perfil/senha")
    public String alterarSenha(@RequestParam("senhaAtual") String senhaAtual, 
                               @RequestParam("novaSenha") String novaSenha,
                               @AuthenticationPrincipal UsuarioLogado usuarioLogado, 
                               RedirectAttributes attributes) {
        
        // CORRIGIDO: getIdUsuario() em vez de getId()
        Usuario usuario = usuarioRepositorio.findById(usuarioLogado.getIdUsuario()).orElse(null);
        
        if (usuario == null) {
            attributes.addFlashAttribute("mensagem", "Erro crítico: Usuário não localizado.");
            attributes.addFlashAttribute("tipoMensagem", "danger");
            return "redirect:/perfil";
        }

        // Validação: A senha atual digitada precisa bater com o hash do banco
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            attributes.addFlashAttribute("mensagem", "A senha atual informada está incorreta.");
            attributes.addFlashAttribute("tipoMensagem", "danger");
            return "redirect:/perfil";
        }

        // Sucesso: Criptografa a nova senha antes de persistir
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepositorio.save(usuario);

        attributes.addFlashAttribute("mensagem", "Senha alterada com sucesso! Utilize-a no próximo login.");
        attributes.addFlashAttribute("tipoMensagem", "success");
        
        return "redirect:/perfil";
    }
}