package com.projeto.sistema.controle;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
    private GrupoRepositorio grupoRepositorio; // Adicionado aqui no topo para organizar

    @GetMapping("/cadastroContatos")
    public ModelAndView cadastrar(Contatos contatos) {
        ModelAndView mv = new ModelAndView("contatos/cadastroContatos");
        mv.addObject("contato", contatos);
        mv.addObject("listaEstados", UF.values());
        return mv;
    }

 
    @GetMapping("/listarContatos")
    public ModelAndView listar(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "cidade", required = false) String cidade,
            @RequestParam(value = "grupoId", required = false) Long grupoId) {
        
        ModelAndView mv = new ModelAndView("contatos/lista");

        // AQUI ESTÁ A MUDANÇA: Usamos o método novo 'filtrarBusca'
        // Ele vai buscar por nome E cidade E grupo, tudo junto.
        mv.addObject("listaContatos", contatosRepositorio.filtrarBusca(nome, cidade, grupoId));

        mv.addObject("listaEstados", UF.values());
        mv.addObject("listaGrupos", grupoRepositorio.findAll());
        mv.addObject("grupoSelecionado", grupoId); 

        return mv;
    }

    @GetMapping("/editarContatos/{id}")
    public ModelAndView editar(@PathVariable("id") Long id) {
        Optional<Contatos> contatos = contatosRepositorio.findById(id);
        
        // Proteção contra erro 500 (No value present)
        if (contatos.isEmpty()) {
            return new ModelAndView("redirect:/listarContatos");
        }
        
        return cadastrar(contatos.get());
    }
    @GetMapping("/removerContatos/{id}")
    public ModelAndView remover(@PathVariable("id") Long id) {
        Optional<Contatos> contato = contatosRepositorio.findById(id);
        if (contato.isPresent()) {
            contatosRepositorio.delete(contato.get());
        }
        // Redireciona para o listar (que agora aceita parâmetros, mas aqui chamamos sem nenhum para listar tudo)
        return new ModelAndView("redirect:/listarContatos");
    }

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