package com.projeto.sistema.controle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.projeto.sistema.modelos.Usuario;
import com.projeto.sistema.modelos.UsuarioLogado;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- LISTAR ---
    @PreAuthorize("hasAuthority('USUARIO_VISUALIZAR')")
    @GetMapping
    public ModelAndView listarUsuarios(@AuthenticationPrincipal UsuarioLogado logado) {
        ModelAndView mv = new ModelAndView("administrativo/usuarios/lista");

        if (logado.isSuperAdmin()) {
            mv.addObject("listaUsuarios", usuarioRepositorio.findAll());
        } else {
            // Tenant Admin: vê apenas usuários da própria empresa
            mv.addObject("listaUsuarios", usuarioRepositorio.findByEmpresa(logado.getEmpresa()));
        }

        mv.addObject("paginaAtiva", "usuarios");
        return mv;
    }

    // --- NOVO USUÁRIO (formulário) ---
    @PreAuthorize("hasAuthority('USUARIO_CRIAR')")
    @GetMapping("/novo")
    public ModelAndView novoUsuario(@AuthenticationPrincipal UsuarioLogado logado) {
        ModelAndView mv = new ModelAndView("administrativo/usuarios/cadastro");
        mv.addObject("usuario", new Usuario());

        // Perfis: Super Admin vê todos; Tenant Admin vê os da sua empresa + globais
        if (logado.isSuperAdmin()) {
            mv.addObject("listaPerfis", perfilRepositorio.findAll());
            mv.addObject("listaEmpresas", empresaService.listarTodas());
            mv.addObject("isSuperAdmin", true);
        } else {
            mv.addObject("listaPerfis", perfilRepositorio.findByEmpresaOrGlobal(logado.getEmpresa()));
            // Tenant Admin NÃO vê campo empresa — o backend impõe a sua empresa
            mv.addObject("isSuperAdmin", false);
        }

        mv.addObject("paginaAtiva", "usuarios");
        return mv;
    }

    // --- SALVAR (criar + editar) ---
    @PreAuthorize("hasAnyAuthority('USUARIO_CRIAR', 'USUARIO_EDITAR')")
    @PostMapping("/salvar")
    public String salvarUsuario(Usuario usuario, RedirectAttributes attributes,
            @AuthenticationPrincipal UsuarioLogado logado) {

        try {
            // === TRAVA MULTI-TENANT ===
            if (!logado.isSuperAdmin()) {
                // Tenant Admin nunca pode escolher a empresa — forçamos a sua
                usuario.setEmpresa(logado.getEmpresa());
            }

            // Validações básicas
            if (usuario.getEmpresa() == null || usuario.getEmpresa().getId() == null) {
                attributes.addFlashAttribute("mensagemErro",
                        "❌ Erro: Você precisa selecionar uma empresa válida para o usuário.");
                return "redirect:/administrativo/usuarios/novo";
            }

            if (isBlank(usuario.getNome())) {
                attributes.addFlashAttribute("mensagemErro", "❌ Erro: O nome completo é obrigatório.");
                return "redirect:/administrativo/usuarios/novo";
            }

            if (isBlank(usuario.getUsername())) {
                attributes.addFlashAttribute("mensagemErro", "❌ Erro: O nome de login é obrigatório.");
                return "redirect:/administrativo/usuarios/novo";
            }

            if (isBlank(usuario.getEmail())) {
                attributes.addFlashAttribute("mensagemErro", "❌ Erro: O e-mail é obrigatório.");
                return "redirect:/administrativo/usuarios/novo";
            }

            if (usuario.getPerfil() == null || usuario.getPerfil().getId() == null) {
                attributes.addFlashAttribute("mensagemErro", "❌ Erro: Você precisa selecionar um perfil de acesso.");
                return "redirect:/administrativo/usuarios/novo";
            }

            // === LÓGICA DE SENHA ===
            if (usuario.getId() == null) {
                // Novo usuário
                if (isBlank(usuario.getSenha())) {
                    attributes.addFlashAttribute("mensagemErro",
                            "❌ Erro: A senha é obrigatória para novos usuários.");
                    return "redirect:/administrativo/usuarios/novo";
                }
                if (usuario.getSenha().length() < 6) {
                    attributes.addFlashAttribute("mensagemErro",
                            "❌ Erro: A senha deve ter no mínimo 6 caracteres.");
                    return "redirect:/administrativo/usuarios/novo";
                }
                usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
            } else {
                // Edição
                // Garante que o Tenant Admin não edita usuário de outra empresa
                if (!logado.isSuperAdmin()) {
                    Usuario usuarioExistente = usuarioRepositorio.findById(usuario.getId()).orElse(null);
                    if (usuarioExistente == null ||
                            !usuarioExistente.getEmpresa().getId().equals(logado.getEmpresa().getId())) {
                        attributes.addFlashAttribute("mensagemErro",
                                "❌ Acesso negado: Você não pode editar usuários de outra empresa.");
                        return "redirect:/administrativo/usuarios";
                    }
                }

                Usuario usuarioAntigo = usuarioRepositorio.findById(usuario.getId()).orElse(null);
                if (usuarioAntigo != null) {
                    if (!isBlank(usuario.getSenha())) {
                        if (usuario.getSenha().length() < 6) {
                            attributes.addFlashAttribute("mensagemErro",
                                    "❌ Erro: A senha deve ter no mínimo 6 caracteres.");
                            return "redirect:/administrativo/usuarios/novo";
                        }
                        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
                    } else {
                        usuario.setSenha(usuarioAntigo.getSenha());
                    }
                }
            }

            usuarioRepositorio.save(usuario);
            attributes.addFlashAttribute("mensagemSucesso", "✅ Usuário salvo com sucesso!");
            return "redirect:/administrativo/usuarios";

        } catch (DataIntegrityViolationException e) {
            attributes.addFlashAttribute("mensagemErro",
                    "❌ Erro: A empresa selecionada não existe ou ocorreu violação de unicidade.");
            return "redirect:/administrativo/usuarios/novo";
        } catch (Exception e) {
            attributes.addFlashAttribute("mensagemErro", "❌ Erro ao salvar usuário: " + e.getMessage());
            return "redirect:/administrativo/usuarios/novo";
        }
    }

    // --- EDITAR ---
    @PreAuthorize("hasAuthority('USUARIO_EDITAR')")
    @GetMapping("/editar/{id}")
    public ModelAndView editarUsuario(@PathVariable("id") Long id,
            @AuthenticationPrincipal UsuarioLogado logado) {

        Usuario usuario = usuarioRepositorio.findById(id).orElse(null);

        if (usuario == null) {
            return new ModelAndView("redirect:/administrativo/usuarios");
        }

        // Tenant Admin não pode editar usuário de outra empresa
        if (!logado.isSuperAdmin() && (usuario.getEmpresa() == null ||
                !usuario.getEmpresa().getId().equals(logado.getEmpresa().getId()))) {
            return new ModelAndView("redirect:/administrativo/usuarios");
        }

        ModelAndView mv = new ModelAndView("administrativo/usuarios/cadastro");
        mv.addObject("usuario", usuario);
        mv.addObject("paginaAtiva", "usuarios");

        if (logado.isSuperAdmin()) {
            mv.addObject("listaPerfis", perfilRepositorio.findAll());
            mv.addObject("listaEmpresas", empresaService.listarTodas());
            mv.addObject("isSuperAdmin", true);
        } else {
            mv.addObject("listaPerfis", perfilRepositorio.findByEmpresaOrGlobal(logado.getEmpresa()));
            mv.addObject("isSuperAdmin", false);
        }

        return mv;
    }

    // --- REMOVER ---
    @PreAuthorize("hasAuthority('USUARIO_EXCLUIR')")
    @GetMapping("/remover/{id}")
    public String removerUsuario(@PathVariable("id") Long id,
            @AuthenticationPrincipal UsuarioLogado logado,
            RedirectAttributes attributes) {

        Usuario usuario = usuarioRepositorio.findById(id).orElse(null);
        if (usuario == null) {
            return "redirect:/administrativo/usuarios";
        }

        // Tenant Admin não pode excluir usuário de outra empresa
        if (!logado.isSuperAdmin() && (usuario.getEmpresa() == null ||
                !usuario.getEmpresa().getId().equals(logado.getEmpresa().getId()))) {
            attributes.addFlashAttribute("mensagemErro",
                    "❌ Acesso negado: Você não pode excluir usuários de outra empresa.");
            return "redirect:/administrativo/usuarios";
        }

        usuarioRepositorio.deleteById(id);
        attributes.addFlashAttribute("mensagemSucesso", "✅ Usuário removido com sucesso.");
        return "redirect:/administrativo/usuarios";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}