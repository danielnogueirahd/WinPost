package com.projeto.sistema.controle;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.projeto.sistema.modelos.Contatos;
import com.projeto.sistema.repositorios.ContatosRepositorio;
import jakarta.validation.Valid;
import com.projeto.sistema.modelos.UF;

@Controller
public class ContatosControle {

	@Autowired
	private ContatosRepositorio contatosRepositorio;

	@GetMapping("/cadastroContatos")
	public ModelAndView cadastrar(Contatos contatos) {
		ModelAndView mv = new ModelAndView("contatos/cadastroContatos");
		mv.addObject("contato", contatos);
		
		System.out.println("---- CARREGANDO LISTA DE ESTADOS ----");
		System.out.println("Total de estados: " + UF.values().length);

		mv.addObject("listaEstados", UF.values());
		return mv;
	}

	@GetMapping("/listarContatos")
	public ModelAndView listar() {
	    
	    ModelAndView mv = new ModelAndView("contatos/lista"); 
	    mv.addObject("listaContatos", contatosRepositorio.findAll());
	    mv.addObject("listaEstados", UF.values());

	    return mv;
	}

	@GetMapping("/editarContatos/{id}")
	public ModelAndView editar(@PathVariable("id") Long id) {
		Optional<Contatos> contatos = contatosRepositorio.findById(id);
		return cadastrar(contatos.get());
	}

	@GetMapping("/removerContatos/{id}")
	public ModelAndView remover(@PathVariable("id") Long id) {
		Optional<Contatos> contato = contatosRepositorio.findById(id);
		if (contato.isPresent()) {
			contatosRepositorio.delete(contato.get());
		}
		return listar();
	}

	@PostMapping("/salvarContato")
	public ModelAndView salvar(@Valid @ModelAttribute("contato") Contatos contatos, BindingResult result, RedirectAttributes attributes) {

		if (result.hasErrors()) {
			return cadastrar(contatos);
		}

		contatosRepositorio.saveAndFlush(contatos);
		
		// Define a mensagem temporária que aparecerá na próxima tela
		attributes.addFlashAttribute("mensagemSucesso", "Contato cadastrado com sucesso!");
		
		// Redireciona para a rota /listarContatos
		return new ModelAndView("redirect:/listarContatos");
	}
	
	
}