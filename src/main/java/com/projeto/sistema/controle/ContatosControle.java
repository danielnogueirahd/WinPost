package com.projeto.sistema.controle;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.projeto.sistema.modelos.Contatos;
import com.projeto.sistema.modelos.UF;
import com.projeto.sistema.modelos.UsuarioLogado;
import com.projeto.sistema.repositorios.ContatosRepositorio;
import com.projeto.sistema.repositorios.GrupoRepositorio;
import com.projeto.sistema.servicos.ContatosService;

import jakarta.validation.Valid;

@Controller
public class ContatosControle {

    @Autowired
    private ContatosRepositorio contatosRepositorio;

    @Autowired
    private ContatosService contatosService;

    @Autowired
    private GrupoRepositorio grupoRepositorio;

    // 1. TELA DE CADASTRO
    @PreAuthorize("hasAuthority('CONTATO_CRIAR')")
    @GetMapping("/cadastroContatos")
    public ModelAndView cadastrar(Contatos contatos, @AuthenticationPrincipal UsuarioLogado usuarioLogado) {
        ModelAndView mv = new ModelAndView("contatos/cadastroContatos");
        mv.addObject("contato", contatos);
        mv.addObject("listaEstados", UF.values());
        // Super Admin pode selecionar empresa no formulário
        mv.addObject("isSuperAdmin", usuarioLogado.isSuperAdmin());
        return mv;
    }

    // 2. LISTAR (filtrado por empresa)
    @PreAuthorize("hasAuthority('CONTATO_VISUALIZAR')")
    @GetMapping("/listarContatos")
    public ModelAndView listar(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "cidade", required = false) String cidade,
            @RequestParam(value = "grupoId", required = false) Long grupoId,
            @AuthenticationPrincipal UsuarioLogado usuarioLogado) {

        ModelAndView mv = new ModelAndView("contatos/lista");

        mv.addObject("listaContatos",
                contatosService.buscar(nome, cidade, grupoId, usuarioLogado.getEmpresa()));

        mv.addObject("listaEstados", UF.values());

        // Grupos filtrados pela empresa do usuário
        if (usuarioLogado.isSuperAdmin()) {
            mv.addObject("listaGrupos", grupoRepositorio.findAll());
        } else {
            mv.addObject("listaGrupos", grupoRepositorio.findByEmpresa(usuarioLogado.getEmpresa()));
        }

        mv.addObject("grupoSelecionado", grupoId);
        return mv;
    }

    // 3. EDITAR
    @PreAuthorize("hasAuthority('CONTATO_EDITAR')")
    @GetMapping("/editarContatos/{id}")
    public ModelAndView editar(@PathVariable("id") Long id,
            @AuthenticationPrincipal UsuarioLogado usuarioLogado) {

        Optional<Contatos> contatos = contatosRepositorio.findById(id);

        if (contatos.isEmpty()) {
            return new ModelAndView("redirect:/listarContatos");
        }

        // Tenant Admin não pode editar contato de outra empresa
        if (!usuarioLogado.isSuperAdmin()) {
            if (contatos.get().getEmpresa() == null ||
                    !contatos.get().getEmpresa().getId().equals(usuarioLogado.getEmpresa().getId())) {
                return new ModelAndView("redirect:/listarContatos");
            }
        }

        return cadastrar(contatos.get(), usuarioLogado);
    }

    // 4. REMOVER
    @PreAuthorize("hasAuthority('CONTATO_EXCLUIR')")
    @GetMapping("/removerContatos/{id}")
    public ModelAndView remover(@PathVariable("id") Long id,
            @AuthenticationPrincipal UsuarioLogado usuarioLogado) {

        contatosService.excluirComSeguranca(id, usuarioLogado.getEmpresa());
        return new ModelAndView("redirect:/listarContatos");
    }

    // 5. SALVAR
    @PreAuthorize("hasAnyAuthority('CONTATO_CRIAR', 'CONTATO_EDITAR')")
    @PostMapping("/salvarContato")
    public ModelAndView salvar(
            @Valid @ModelAttribute("contato") Contatos contatos,
            BindingResult result,
            RedirectAttributes attributes,
            @AuthenticationPrincipal UsuarioLogado usuarioLogado) {

        if (result.hasErrors()) {
            return cadastrar(contatos, usuarioLogado);
        }

        contatosService.salvar(contatos);

        attributes.addFlashAttribute("mensagemSucesso", "Contato cadastrado com sucesso!");
        return new ModelAndView("redirect:/listarContatos");
    }
}