package com.projeto.sistema.controle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.projeto.sistema.modelos.Usuario;
import com.projeto.sistema.repositorios.PerfilRepositorio;
import com.projeto.sistema.repositorios.UsuarioRepositorio;
import com.projeto.sistema.servicos.EmpresaService;

@Controller
@RequestMapping("/administrativo/usuarios")
public class UsuarioControle {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    
    @Autowired
    private PerfilRepositorio perfilRepositorio;

    @Autowired
    private EmpresaService empresaService;

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

    @PreAuthorize("hasAuthority('USUARIO_CRIAR')")
    @GetMapping("/novo")
    public ModelAndView novoUsuario() {
        ModelAndView mv = new ModelAndView("administrativo/usuarios/cadastro");
        mv.addObject("usuario", new Usuario());
        mv.addObject("listaPerfis", perfilRepositorio.findAll());
        
        // Esta linha é obrigatória para o dropdown de empresas aparecer!
        mv.addObject("listaEmpresas", empresaService.listarTodas()); 
        
        mv.addObject("paginaAtiva", "usuarios");
        return mv;
    }

    // FECHADURA: Tanto quem CRIA quanto quem EDITA pode salvar
    @PreAuthorize("hasAnyAuthority('USUARIO_CRIAR', 'USUARIO_EDITAR')")
    @PostMapping("/salvar")
    public String salvarUsuario(Usuario usuario, RedirectAttributes attributes) {
        
        try {
            // 1. Validação: empresa não selecionada
            if (usuario.getEmpresa() == null || usuario.getEmpresa().getId() == null) {
                attributes.addFlashAttribute("mensagemErro", 
                    "❌ Erro: Você precisa selecionar uma empresa válida para o usuário.");
                return "redirect:/administrativo/usuarios/novo";
            }

            // 2. Validação: nome vazio
            if (usuario.getNome() == null || usuario.getNome().trim().isEmpty()) {
                attributes.addFlashAttribute("mensagemErro", 
                    "❌ Erro: O nome completo é obrigatório.");
                return "redirect:/administrativo/usuarios/novo";
            }

            // 3. Validação: username vazio
            if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
                attributes.addFlashAttribute("mensagemErro", 
                    "❌ Erro: O nome de login é obrigatório.");
                return "redirect:/administrativo/usuarios/novo";
            }

            // 4. Validação: email vazio
            if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
                attributes.addFlashAttribute("mensagemErro", 
                    "❌ Erro: O e-mail é obrigatório.");
                return "redirect:/administrativo/usuarios/novo";
            }

            // 5. Validação: perfil não selecionado
            if (usuario.getPerfil() == null || usuario.getPerfil().getId() == null) {
                attributes.addFlashAttribute("mensagemErro", 
                    "❌ Erro: Você precisa selecionar um perfil de acesso.");
                return "redirect:/administrativo/usuarios/novo";
            }

            // 6. Lógica de criptografia de senha
            if (usuario.getId() == null) {
                // Novo Usuário - senha obrigatória
                if (usuario.getSenha() == null || usuario.getSenha().trim().isEmpty()) {
                    attributes.addFlashAttribute("mensagemErro", 
                        "❌ Erro: A palavra-passe é obrigatória para novos usuários.");
                    return "redirect:/administrativo/usuarios/novo";
                }
                
                if (usuario.getSenha().length() < 6) {
                    attributes.addFlashAttribute("mensagemErro", 
                        "❌ Erro: A palavra-passe deve ter no mínimo 6 caracteres.");
                    return "redirect:/administrativo/usuarios/novo";
                }
                
                String senhaCriptografada = passwordEncoder.encode(usuario.getSenha());
                usuario.setSenha(senhaCriptografada);
            } else {
                // Edição: Busca o atual para preservar senha se o campo vier vazio
                Usuario usuarioAntigo = usuarioRepositorio.findById(usuario.getId()).orElse(null);
                if (usuarioAntigo != null) {
                    if (usuario.getSenha() != null && !usuario.getSenha().trim().isEmpty()) {
                        if (usuario.getSenha().length() < 6) {
                            attributes.addFlashAttribute("mensagemErro", 
                                "❌ Erro: A palavra-passe deve ter no mínimo 6 caracteres.");
                            return "redirect:/administrativo/usuarios/novo";
                        }
                        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
                    } else {
                        usuario.setSenha(usuarioAntigo.getSenha());
                    }
                }
            }

            // 7. Salva no banco
            usuarioRepositorio.save(usuario);
            attributes.addFlashAttribute("mensagemSucesso", 
                "✅ Usuário salvo com sucesso!");
            
            // Redireciona para a lista de usuários (não para perfis!)
            return "redirect:/administrativo/usuarios";

        } catch (DataIntegrityViolationException e) {
            // Erro de Foreign Key - empresa não existe
            attributes.addFlashAttribute("mensagemErro", 
                "❌ Erro: A empresa selecionada não existe no sistema. Contate o administrador.");
            System.err.println("Erro de Foreign Key: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/administrativo/usuarios/novo";
            
        } catch (Exception e) {
            attributes.addFlashAttribute("mensagemErro", 
                "❌ Erro ao salvar usuário: " + e.getMessage());
            System.err.println("Erro geral ao salvar usuário: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/administrativo/usuarios/novo";
        }
    }

    @PreAuthorize("hasAuthority('USUARIO_EDITAR')")
    @GetMapping("/editar/{id}")
    public ModelAndView editarUsuario(@PathVariable("id") Long id) {
        ModelAndView mv = new ModelAndView("administrativo/usuarios/cadastro");

        mv.addObject("usuario", usuarioRepositorio.findById(id).orElse(new Usuario()));
        mv.addObject("listaPerfis", perfilRepositorio.findAll());

        // ADICIONE ESTA LINHA TAMBÉM:
        mv.addObject("listaEmpresas", empresaService.listarTodas());

        mv.addObject("paginaAtiva", "usuarios");

        return mv;
    }

    // FECHADURA: Apenas quem tem permissão para EXCLUIR
    @PreAuthorize("hasAuthority('USUARIO_EXCLUIR')")
    @GetMapping("/remover/{id}")
    public String removerUsuario(@PathVariable("id") Long id) {
        usuarioRepositorio.deleteById(id);
        return "redirect:/administrativo/usuarios";
    }
}