package com.projeto.sistema.controle;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // IMPORT DO CRACHÁ
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
import com.projeto.sistema.modelos.UsuarioLogado; // IMPORT DO CRACHÁ
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

	// 1. MÉTODO CADASTRAR (Abre a tela limpa ou com erros)
	@PreAuthorize("hasAuthority('CONTATO_CRIAR')")
	@GetMapping("/cadastroContatos")
	public ModelAndView cadastrar(Contatos contatos) {
		ModelAndView mv = new ModelAndView("contatos/cadastroContatos");
		mv.addObject("contato", contatos);
		mv.addObject("listaEstados", UF.values());
		return mv;
	}

	// 2. MÉTODO LISTAR (Filtra pela empresa do usuário logado)
	@PreAuthorize("hasAuthority('CONTATO_VISUALIZAR')")
	@GetMapping("/listarContatos")
	public ModelAndView listar(@RequestParam(value = "nome", required = false) String nome,
			@RequestParam(value = "cidade", required = false) String cidade,
			@RequestParam(value = "grupoId", required = false) Long grupoId,
			@AuthenticationPrincipal UsuarioLogado usuarioLogado) { // <-- LÊ O CRACHÁ

		ModelAndView mv = new ModelAndView("contatos/lista");

		// <-- AGORA CHAMA O SERVIÇO (QUE LIMPA OS DADOS) EM VEZ DO REPOSITÓRIO DIRETO
		mv.addObject("listaContatos", contatosService.buscar(nome, cidade, grupoId, usuarioLogado.getEmpresa()));

		mv.addObject("listaEstados", UF.values());

		// NOTA DE SEGURANÇA: Aqui também deveria puxar apenas os grupos da empresa!
		// Se já tiver o método criado no GrupoRepositorio, use:
		// grupoRepositorio.findByEmpresa(usuarioLogado.getEmpresa()) em vez de
		// findAll()
		mv.addObject("listaGrupos", grupoRepositorio.findAll());

		mv.addObject("grupoSelecionado", grupoId);

		return mv;
	}

	// 3. MÉTODO EDITAR (Traz os dados de um contato existente)
	@PreAuthorize("hasAuthority('CONTATO_EDITAR')")
	@GetMapping("/editarContatos/{id}")
	public ModelAndView editar(@PathVariable("id") Long id, @AuthenticationPrincipal UsuarioLogado usuarioLogado) {
		Optional<Contatos> contatos = contatosRepositorio.findById(id);

		if (contatos.isEmpty()) {
			return new ModelAndView("redirect:/listarContatos");
		}

		// SEGURANÇA: Garante que o contato que está a ser editado pertence à mesma
		// empresa do utilizador!
		if (!contatos.get().getEmpresa().getId().equals(usuarioLogado.getEmpresa().getId())) {
			return new ModelAndView("redirect:/listarContatos");
		}

		return cadastrar(contatos.get()); // Aqui chama o método cadastrar sem erros!
	}

	// 4. MÉTODO REMOVER
	@PreAuthorize("hasAuthority('CONTATO_EXCLUIR')")
	@GetMapping("/removerContatos/{id}")
	public ModelAndView remover(@PathVariable("id") Long id, @AuthenticationPrincipal UsuarioLogado usuarioLogado) {
		Optional<Contatos> contato = contatosRepositorio.findById(id);

		if (contato.isPresent()) {
			// SEGURANÇA: Garante que o utilizador só apaga contatos da própria empresa
			if (contato.get().getEmpresa().getId().equals(usuarioLogado.getEmpresa().getId())) {
				contatosRepositorio.delete(contato.get());
			}
		}
		return new ModelAndView("redirect:/listarContatos");
	}

	// 5. MÉTODO SALVAR (Grava no banco de dados)
	@PreAuthorize("hasAnyAuthority('CONTATO_CRIAR', 'CONTATO_EDITAR')")
	@PostMapping("/salvarContato")
	public ModelAndView salvar(@Valid @ModelAttribute("contato") Contatos contatos, BindingResult result,
			RedirectAttributes attributes, @AuthenticationPrincipal UsuarioLogado usuarioLogado) { // <-- LÊ O CRACHÁ

		if (result.hasErrors()) {
			return cadastrar(contatos); // Volta para a tela de erro
		}

		// <-- ANTES DE GRAVAR, CARIMBA A EMPRESA DO USUÁRIO LOGADO NO CONTATO
		contatos.setEmpresa(usuarioLogado.getEmpresa());

		contatosRepositorio.saveAndFlush(contatos);

		attributes.addFlashAttribute("mensagemSucesso", "Contato cadastrado com sucesso!");

		return new ModelAndView("redirect:/listarContatos");
	}
}