package com.projeto.sistema.controle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.projeto.sistema.modelos.Perfil;
import com.projeto.sistema.modelos.Permissao;
import com.projeto.sistema.repositorios.PerfilRepositorio;

@Controller
@RequestMapping("/administrativo/perfis")
public class PerfilControle {

	@Autowired
	private PerfilRepositorio perfilRepositorio;

	// FECHADURA TEMPORARIAMENTE DESLIGADA PARA O ADMIN ENTRAR
	// @PreAuthorize("hasAuthority('PERFIL_VISUALIZAR')")
	@GetMapping
	public ModelAndView listarPerfis() {
		ModelAndView mv = new ModelAndView("administrativo/perfis/lista");
		mv.addObject("listaPerfis", perfilRepositorio.findAll());
		mv.addObject("paginaAtiva", "perfis");
		return mv;
	}

	// FECHADURA TEMPORARIAMENTE DESLIGADA PARA O ADMIN CRIAR O PRIMEIRO
	// @PreAuthorize("hasAuthority('PERFIL_CRIAR')") <-- AQUI ESTAVA O BLOQUEIO!
	@GetMapping("/novo")
	public ModelAndView novoPerfil() {
		ModelAndView mv = new ModelAndView("administrativo/perfis/cadastro");
		mv.addObject("perfil", new Perfil());
		mv.addObject("todasPermissoes", Permissao.values());
		mv.addObject("paginaAtiva", "perfis");
		return mv;
	}

	// FECHADURA TEMPORARIAMENTE DESLIGADA PARA O ADMIN SALVAR
	// @PreAuthorize("hasAnyAuthority('PERFIL_CRIAR', 'PERFIL_EDITAR')") <-- AQUI TAMBÉM!
	@PostMapping("/salvar")
	public String salvarPerfil(Perfil perfil) {
		perfilRepositorio.save(perfil);
		return "redirect:/administrativo/perfis";
	}

	// FECHADURA TEMPORARIAMENTE DESLIGADA PARA O ADMIN EDITAR
	// @PreAuthorize("hasAuthority('PERFIL_EDITAR')")
	@GetMapping("/editar/{id}")
	public ModelAndView editarPerfil(@PathVariable("id") Long id) {
		ModelAndView mv = new ModelAndView("administrativo/perfis/cadastro");

		mv.addObject("perfil", perfilRepositorio.findById(id).orElse(new Perfil()));
		mv.addObject("todasPermissoes", Permissao.values());
		mv.addObject("paginaAtiva", "perfis");

		return mv;
	}

	// FECHADURA: Apenas quem pode EXCLUIR
	// @PreAuthorize("hasAuthority('PERFIL_EXCLUIR')") <-- Comentei também para você conseguir apagar se errar algo agora
	@GetMapping("/remover/{id}")
	public String removerPerfil(@PathVariable("id") Long id) {
		// ATENÇÃO: Na vida real, você não pode excluir um perfil se houver usuários
		// usando ele.
		// O SQL Server vai bloquear automaticamente se houver essa dependência.
		perfilRepositorio.deleteById(id);
		return "redirect:/administrativo/perfis";
	}
}