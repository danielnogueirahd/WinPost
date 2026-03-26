package com.projeto.sistema.controle;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize; // <-- NOVO IMPORT DE SEGURANÇA
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
import com.projeto.sistema.repositorios.ContatosRepositorio;
import com.projeto.sistema.repositorios.GrupoRepositorio;

import jakarta.validation.Valid;

@Controller
public class ContatosControle {

    @Autowired
    private ContatosRepositorio contatosRepositorio;

    @Autowired
    private GrupoRepositorio grupoRepositorio;

    // FECHADURA: Só entra quem tem poder para CRIAR contactos
    @PreAuthorize("hasAuthority('CONTATO_CRIAR')")
    @GetMapping("/cadastroContatos")
    public ModelAndView cadastrar(Contatos contatos) {
        ModelAndView mv = new ModelAndView("contatos/cadastroContatos");
        mv.addObject("contato", contatos);
        mv.addObject("listaEstados", UF.values());
        return mv;
    }

    // FECHADURA: Só entra quem tem poder para VISUALIZAR contactos
    @PreAuthorize("hasAuthority('CONTATO_VISUALIZAR')")
    @GetMapping("/listarContatos")
    public ModelAndView listar(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "cidade", required = false) String cidade,
            @RequestParam(value = "grupoId", required = false) Long grupoId) {
        
        ModelAndView mv = new ModelAndView("contatos/lista");

        mv.addObject("listaContatos", contatosRepositorio.filtrarBusca(nome, cidade, grupoId));
        mv.addObject("listaEstados", UF.values());
        mv.addObject("listaGrupos", grupoRepositorio.findAll());
        mv.addObject("grupoSelecionado", grupoId); 

        return mv;
    }

    // FECHADURA: Só entra quem tem poder para EDITAR contactos
    @PreAuthorize("hasAuthority('CONTATO_EDITAR')")
    @GetMapping("/editarContatos/{id}")
    public ModelAndView editar(@PathVariable("id") Long id) {
        Optional<Contatos> contatos = contatosRepositorio.findById(id);
        
        if (contatos.isEmpty()) {
            return new ModelAndView("redirect:/listarContatos");
        }
        
        return cadastrar(contatos.get());
    }

    // FECHADURA: Só entra quem tem poder para EXCLUIR contactos
    @PreAuthorize("hasAuthority('CONTATO_EXCLUIR')")
    @GetMapping("/removerContatos/{id}")
    public ModelAndView remover(@PathVariable("id") Long id) {
        Optional<Contatos> contato = contatosRepositorio.findById(id);
        if (contato.isPresent()) {
            contatosRepositorio.delete(contato.get());
        }
        return new ModelAndView("redirect:/listarContatos");
    }

    // FECHADURA: Como o botão "Salvar" serve tanto para criar um novo como para atualizar um existente, 
    // verificamos se o utilizador tem pelo menos uma das duas permissões
    @PreAuthorize("hasAnyAuthority('CONTATO_CRIAR', 'CONTATO_EDITAR')")
    @PostMapping("/salvarContato")
    public ModelAndView salvar(@Valid @ModelAttribute("contato") Contatos contatos, BindingResult result, RedirectAttributes attributes) {

        if (result.hasErrors()) {
            return cadastrar(contatos);
        }

        contatosRepositorio.saveAndFlush(contatos);
        
        attributes.addFlashAttribute("mensagemSucesso", "Contato cadastrado com sucesso!");
        
        return new ModelAndView("redirect:/listarContatos");
    }
}