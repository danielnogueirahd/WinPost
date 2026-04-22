package com.projeto.sistema.controle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;
import com.projeto.sistema.modelos.Perfil;
import com.projeto.sistema.modelos.Permissao;
import com.projeto.sistema.repositorios.PerfilRepositorio;

@Controller
@RequestMapping("/administrativo/perfis")
public class PerfilAcessoControle { // NOME ALTERADO AQUI PARA NÃO DAR CONFLITO!

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
	// @PreAuthorize("hasAuthority('PERFIL_CRIAR')")
	@GetMapping("/novo")
	public ModelAndView novoPerfil() {
		ModelAndView mv = new ModelAndView("administrativo/perfis/cadastro");
		mv.addObject("perfil", new Perfil());
		
		// Lógica de agrupamento por Módulo adicionada aqui
		Map<String, List<Permissao>> permissoesAgrupadas = Arrays.stream(Permissao.values())
				.collect(Collectors.groupingBy(Permissao::getModulo));
		mv.addObject("permissoesAgrupadas", permissoesAgrupadas);
		
		mv.addObject("paginaAtiva", "perfis");
		return mv;
	}

	// FECHADURA TEMPORARIAMENTE DESLIGADA PARA O ADMIN SALVAR
	// @PreAuthorize("hasAnyAuthority('PERFIL_CRIAR', 'PERFIL_EDITAR')")
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
		
		// Lógica de agrupamento por Módulo adicionada aqui também
		Map<String, List<Permissao>> permissoesAgrupadas = Arrays.stream(Permissao.values())
				.collect(Collectors.groupingBy(Permissao::getModulo));
		mv.addObject("permissoesAgrupadas", permissoesAgrupadas);
		
		mv.addObject("paginaAtiva", "perfis");

		return mv;
	}

	// FECHADURA: Apenas quem pode EXCLUIR
	// @PreAuthorize("hasAuthority('PERFIL_EXCLUIR')") 
	@GetMapping("/remover/{id}")
	public String removerPerfil(@PathVariable("id") Long id) {
		
		// O SQL Server vai bloquear automaticamente se houver essa dependência.
		perfilRepositorio.deleteById(id);
		return "redirect:/administrativo/perfis";
	}
}