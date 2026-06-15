package com.projeto.sistema.controle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.projeto.sistema.modelos.Perfil;
import com.projeto.sistema.modelos.Permissao;
import com.projeto.sistema.modelos.UsuarioLogado;
import com.projeto.sistema.repositorios.PerfilRepositorio;

@Controller
@RequestMapping("/administrativo/perfis")
public class PerfilAcessoControle {

    @Autowired
    private PerfilRepositorio perfilRepositorio;

    @PreAuthorize("hasAuthority('USUARIO_VISUALIZAR')")
    @GetMapping
    public ModelAndView listarPerfis(@AuthenticationPrincipal UsuarioLogado logado) {
        ModelAndView mv = new ModelAndView("administrativo/perfis/lista");

        if (logado.isSuperAdmin()) {
            mv.addObject("listaPerfis", perfilRepositorio.findAll());
        } else {
            // Tenant Admin vê apenas perfis da sua empresa
            mv.addObject("listaPerfis", perfilRepositorio.findByEmpresa(logado.getEmpresa()));
        }

        mv.addObject("paginaAtiva", "perfis");
        return mv;
    }

    @PreAuthorize("hasAuthority('USUARIO_CRIAR')")
    @GetMapping("/novo")
    public ModelAndView novoPerfil() {
        ModelAndView mv = new ModelAndView("administrativo/perfis/cadastro");
        mv.addObject("perfil", new Perfil());
        mv.addObject("permissoesAgrupadas", getPermissoesAgrupadas());
        mv.addObject("paginaAtiva", "perfis");
        return mv;
    }

    @PreAuthorize("hasAnyAuthority('USUARIO_CRIAR', 'USUARIO_EDITAR')")
    @PostMapping("/salvar")
    public String salvarPerfil(Perfil perfil, @AuthenticationPrincipal UsuarioLogado logado,
            RedirectAttributes attributes) {

        // Tenant Admin: o perfil criado pertence à sua empresa
        if (!logado.isSuperAdmin()) {
            perfil.setEmpresa(logado.getEmpresa());
        }

        perfilRepositorio.save(perfil);
        attributes.addFlashAttribute("mensagemSucesso", "✅ Perfil salvo com sucesso!");
        return "redirect:/administrativo/perfis";
    }

    @PreAuthorize("hasAuthority('USUARIO_EDITAR')")
    @GetMapping("/editar/{id}")
    public ModelAndView editarPerfil(@PathVariable("id") Long id,
            @AuthenticationPrincipal UsuarioLogado logado) {

        Perfil perfil = perfilRepositorio.findById(id).orElse(null);

        if (perfil == null) {
            return new ModelAndView("redirect:/administrativo/perfis");
        }

        // Tenant Admin não pode editar perfil de outra empresa
        if (!logado.isSuperAdmin() && perfil.getEmpresa() != null &&
                !perfil.getEmpresa().getId().equals(logado.getEmpresa().getId())) {
            return new ModelAndView("redirect:/administrativo/perfis");
        }

        ModelAndView mv = new ModelAndView("administrativo/perfis/cadastro");
        mv.addObject("perfil", perfil);
        mv.addObject("permissoesAgrupadas", getPermissoesAgrupadas());
        mv.addObject("paginaAtiva", "perfis");
        return mv;
    }

    @PreAuthorize("hasAuthority('USUARIO_EXCLUIR')")
    @GetMapping("/remover/{id}")
    public String removerPerfil(@PathVariable("id") Long id,
            @AuthenticationPrincipal UsuarioLogado logado,
            RedirectAttributes attributes) {

        Perfil perfil = perfilRepositorio.findById(id).orElse(null);

        if (perfil == null) {
            return "redirect:/administrativo/perfis";
        }

        // Tenant Admin não pode excluir perfil de outra empresa
        if (!logado.isSuperAdmin() && perfil.getEmpresa() != null &&
                !perfil.getEmpresa().getId().equals(logado.getEmpresa().getId())) {
            attributes.addFlashAttribute("mensagemErro",
                    "❌ Acesso negado: Você não pode excluir perfis de outra empresa.");
            return "redirect:/administrativo/perfis";
        }

        perfilRepositorio.deleteById(id);
        attributes.addFlashAttribute("mensagemSucesso", "✅ Perfil removido com sucesso.");
        return "redirect:/administrativo/perfis";
    }

    private Map<String, List<Permissao>> getPermissoesAgrupadas() {
        return Arrays.stream(Permissao.values())
                .collect(Collectors.groupingBy(Permissao::getModulo));
    }
}