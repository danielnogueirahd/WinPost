package com.projeto.sistema.controle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.projeto.sistema.modelos.Usuario;
import com.projeto.sistema.repositorios.PerfilRepositorio;
import com.projeto.sistema.repositorios.UsuarioRepositorio;

@Controller
@RequestMapping("/administrativo/usuarios")
public class UsuarioControle {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private PerfilRepositorio perfilRepositorio;

    // A NOSSA MÁQUINA DE CRIPTOGRAFAR SENHAS
    @Autowired
    private PasswordEncoder passwordEncoder;

    // FECHADURA: Apenas quem tem permissão para VISUALIZAR
    @PreAuthorize("hasAuthority('USUARIO_VISUALIZAR')")
    @GetMapping
    public ModelAndView listarUsuarios() {
        ModelAndView mv = new ModelAndView("administrativo/usuarios/lista");
        mv.addObject("listaUsuarios", usuarioRepositorio.findAll());
        mv.addObject("paginaAtiva", "usuarios"); 
        return mv;
    }

    // FECHADURA: Apenas quem tem permissão para CRIAR
    @PreAuthorize("hasAuthority('USUARIO_CRIAR')")
    @GetMapping("/novo")
    public ModelAndView novoUsuario() {
        ModelAndView mv = new ModelAndView("administrativo/usuarios/cadastro");
        mv.addObject("usuario", new Usuario());
        mv.addObject("listaPerfis", perfilRepositorio.findAll());
        mv.addObject("paginaAtiva", "usuarios");
        return mv;
    }

    // FECHADURA: Apenas quem tem permissão para CRIAR salva novos no banco
    @PreAuthorize("hasAuthority('USUARIO_CRIAR')")
    @PostMapping("/salvar")
    public String salvarUsuario(Usuario usuario, RedirectAttributes attributes) {
        
        // Verifica se é um usuário NOVO (não tem ID ainda)
        if (usuario.getId() == null) {
            // Pega a senha que foi digitada na tela e transforma num código ilegível (Hash)
            String senhaCriptografada = passwordEncoder.encode(usuario.getSenha());
            usuario.setSenha(senhaCriptografada);
        } 
        
        // Salva a pessoa no banco de dados
        usuarioRepositorio.save(usuario);
        
        // Redireciona de volta para a tabela
        return "redirect:/administrativo/usuarios";
    }
}