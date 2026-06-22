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
import com.projeto.sistema.modelos.Empresa;
import com.projeto.sistema.modelos.UF;
import com.projeto.sistema.modelos.UsuarioLogado;
import com.projeto.sistema.repositorios.ContatosRepositorio;
import com.projeto.sistema.repositorios.GrupoRepositorio;
import com.projeto.sistema.servicos.ContatosService;
import com.projeto.sistema.servicos.EmpresaService;

import jakarta.validation.Valid;

@Controller
public class ContatosControle {

	@Autowired
	private ContatosRepositorio contatosRepositorio;

	@Autowired
	private ContatosService contatosService;

	@Autowired
	private GrupoRepositorio grupoRepositorio;

	// ── NOVO: injeção do EmpresaService ──────────────────────────────────────
	@Autowired
	private EmpresaService empresaService;

	// =========================================================================
	// GET — Formulário de cadastro / edição
	// =========================================================================
	@PreAuthorize("hasAuthority('CONTATO_CRIAR')")
	@GetMapping("/cadastroContatos")
	public ModelAndView cadastrar(Contatos contatos, @AuthenticationPrincipal UsuarioLogado usuarioLogado) {

		ModelAndView mv = new ModelAndView("contatos/cadastroContatos");
		mv.addObject("contato", contatos);
		mv.addObject("listaEstados", UF.values());

		// ── REGRA 1: Super Admin recebe a lista de empresas ativas ──────────
		if (usuarioLogado.isSuperAdmin()) {
			mv.addObject("listaEmpresas", empresaService.listarTodas());
		}
		// isSuperAdmin já é exposto globalmente pelo GlobalAtributos,
		// mas adicionamos aqui também para garantir disponibilidade local.
		mv.addObject("isSuperAdmin", usuarioLogado.isSuperAdmin());

		return mv;
	}

	// =========================================================================
	// GET — Editar contato existente
	// =========================================================================
	@PreAuthorize("hasAuthority('CONTATO_EDITAR')")
	@GetMapping("/editarContatos/{id}")
	public ModelAndView editar(@PathVariable("id") Long id, @AuthenticationPrincipal UsuarioLogado usuarioLogado) {

		Optional<Contatos> contatos = contatosRepositorio.findById(id);

		if (contatos.isEmpty()) {
			return new ModelAndView("redirect:/listarContatos");
		}

		// Tenant Admin não pode editar contato de outra empresa
		if (!usuarioLogado.isSuperAdmin()) {
			Empresa empresaDoContato = contatos.get().getEmpresa();
			Empresa empresaDoUsuario = usuarioLogado.getEmpresa();
			if (empresaDoContato == null || !empresaDoContato.getId().equals(empresaDoUsuario.getId())) {
				return new ModelAndView("redirect:/listarContatos");
			}
		}

		return cadastrar(contatos.get(), usuarioLogado);
	}

	// =========================================================================
	// GET — Listar contatos
	// =========================================================================
	@PreAuthorize("hasAuthority('CONTATO_VISUALIZAR')")
	@GetMapping("/listarContatos")
	public ModelAndView listar(@RequestParam(value = "nome", required = false) String nome,
			@RequestParam(value = "cidade", required = false) String cidade,
			@RequestParam(value = "grupoId", required = false) Long grupoId,
			// (Opcional) Podemos capturar o ID da empresa se o Master quiser filtrar por
			// uma empresa específica
			@RequestParam(value = "empresaId", required = false) Long empresaId,
			@AuthenticationPrincipal UsuarioLogado usuarioLogado) {

		ModelAndView mv = new ModelAndView("contatos/lista");

		// REGRA DE LISTAGEM: Super Admin vê tudo (ou filtra por empresa), Usuário comum
		// vê só a sua empresa
		if (usuarioLogado.isSuperAdmin()) {
			// Usa o método que não obriga a ter uma empresa específica vinculada ao usuário
			mv.addObject("listaContatos", contatosRepositorio.filtrarBuscaSuperAdmin(nome, cidade, grupoId, empresaId));
			mv.addObject("listaGrupos", grupoRepositorio.findAll());
			// Envia a lista de empresas para o Master poder filtrar na tela, se quiser
			mv.addObject("listaEmpresas", empresaService.listarTodas());
		} else {
			// Usa o método padrão que trava a visualização na empresa do usuário
			mv.addObject("listaContatos", contatosService.buscar(nome, cidade, grupoId, usuarioLogado.getEmpresa()));
			mv.addObject("listaGrupos", grupoRepositorio.findByEmpresa(usuarioLogado.getEmpresa()));
		}

		mv.addObject("listaEstados", UF.values());
		mv.addObject("grupoSelecionado", grupoId);
		mv.addObject("isSuperAdmin", usuarioLogado.isSuperAdmin());

		return mv;
	}

	// =========================================================================
	// POST — Salvar (criar + editar)
	// =========================================================================
	@PreAuthorize("hasAnyAuthority('CONTATO_CRIAR', 'CONTATO_EDITAR')")
	@PostMapping("/salvarContato")
	public ModelAndView salvar(@Valid @ModelAttribute("contato") Contatos contatos, BindingResult result,
			// ── NOVO: recebe o empresaId apenas quando Super Admin submete ──
			@RequestParam(value = "empresaId", required = false) Long empresaId, RedirectAttributes attributes,
			@AuthenticationPrincipal UsuarioLogado usuarioLogado) {

		if (result.hasErrors()) {
			// Recarrega lista de empresas se houver erro de validação e for SA
			ModelAndView mv = new ModelAndView("contatos/cadastroContatos");
			mv.addObject("contato", contatos);
			mv.addObject("listaEstados", UF.values());
			mv.addObject("isSuperAdmin", usuarioLogado.isSuperAdmin());
			if (usuarioLogado.isSuperAdmin()) {
				mv.addObject("listaEmpresas", empresaService.listarTodas());
			}
			return mv;
		}

		// ── REGRA 2: definição da empresa no contato ─────────────────────────
		if (usuarioLogado.isSuperAdmin()) {
			// Super Admin DEVE ter selecionado uma empresa na tela
			if (empresaId == null) {
				ModelAndView mv = new ModelAndView("contatos/cadastroContatos");
				mv.addObject("contato", contatos);
				mv.addObject("listaEstados", UF.values());
				mv.addObject("isSuperAdmin", true);
				mv.addObject("listaEmpresas", empresaService.listarTodas());
				mv.addObject("erroEmpresa", "Selecione a empresa à qual este contato pertence.");
				return mv;
			}
			// Busca a entidade Empresa pelo ID recebido do formulário
			Empresa empresaSelecionada = empresaService.buscarPorId(empresaId);
			if (empresaSelecionada == null || !empresaSelecionada.isAtivo()) {
				ModelAndView mv = new ModelAndView("contatos/cadastroContatos");
				mv.addObject("contato", contatos);
				mv.addObject("listaEstados", UF.values());
				mv.addObject("isSuperAdmin", true);
				mv.addObject("listaEmpresas", empresaService.listarTodas());
				mv.addObject("erroEmpresa", "Empresa inválida ou inativa. Selecione outra.");
				return mv;
			}
			contatos.setEmpresa(empresaSelecionada);

		} else {
			// Tenant: ignora qualquer empresa que possa ter vindo do request
			// — o backend injeta a empresa do próprio usuário logado
			contatos.setEmpresa(usuarioLogado.getEmpresa());
		}

		contatosService.salvar(contatos);

		attributes.addFlashAttribute("mensagemSucesso", "Contato salvo com sucesso!");
		return new ModelAndView("redirect:/listarContatos");
	}

	// =========================================================================
	// GET — Remover
	// =========================================================================
	@PreAuthorize("hasAuthority('CONTATO_EXCLUIR')")
	@GetMapping("/removerContatos/{id}")
	public ModelAndView remover(@PathVariable("id") Long id, @AuthenticationPrincipal UsuarioLogado usuarioLogado) {

		contatosService.excluirComSeguranca(id, usuarioLogado.getEmpresa());
		return new ModelAndView("redirect:/listarContatos");
	}
}