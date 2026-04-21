package com.projeto.sistema.controle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.projeto.sistema.modelos.Empresa;
import com.projeto.sistema.servicos.EmpresaService;

@Controller
@RequestMapping("/administrativo/empresas")
public class EmpresaControle {

	@Autowired
	private EmpresaService empresaService;

	// 1. LISTAR
	@GetMapping
	@PreAuthorize("hasAuthority('CONFIGURACOES_SISTEMA')")
	public ModelAndView listar() {
		// Atenção ao caminho da View: templates/administrativo/empresas/lista.html
		ModelAndView mv = new ModelAndView("administrativo/empresas/lista");
		mv.addObject("listaEmpresas", empresaService.listarTodas());
		mv.addObject("paginaAtiva", "empresas");
		return mv;
	}

	// 2. ABRIR TELA DE CADASTRO VAZIA
	@GetMapping("/cadastro")
	@PreAuthorize("hasAuthority('CONFIGURACOES_SISTEMA')")
	public ModelAndView cadastrar(Empresa empresa) {
		ModelAndView mv = new ModelAndView("administrativo/empresas/cadastro");
		mv.addObject("empresa", empresa);
		mv.addObject("paginaAtiva", "empresas");
		return mv;
	}

	// 3. SALVAR (Serve para Criar e Editar)
	@PostMapping("/salvar")
	@PreAuthorize("hasAuthority('CONFIGURACOES_SISTEMA')")
	public ModelAndView salvar(Empresa empresa, RedirectAttributes attributes) {
		empresaService.salvar(empresa);
		attributes.addFlashAttribute("mensagemSucesso", "Empresa salva com sucesso!");
		// Redireciona para a listagem
		return new ModelAndView("redirect:/administrativo/empresas");
	}

	// 4. ABRIR TELA DE EDIÇÃO COM DADOS PREENCHIDOS
	@GetMapping("/editar/{id}")
	@PreAuthorize("hasAuthority('CONFIGURACOES_SISTEMA')")
	public ModelAndView editar(@PathVariable("id") Long id) {
		Empresa empresa = empresaService.buscarPorId(id);

		if (empresa == null) {
			return new ModelAndView("redirect:/administrativo/empresas");
		}

		// Reutiliza o método 'cadastrar', mas agora passando uma empresa preenchida
		return cadastrar(empresa);
	}

	// 5. REMOVER
	@GetMapping("/remover/{id}")
	@PreAuthorize("hasAuthority('CONFIGURACOES_SISTEMA')")
	public ModelAndView remover(@PathVariable("id") Long id, RedirectAttributes attributes) {
		try {
			empresaService.excluir(id);
			attributes.addFlashAttribute("mensagemSucesso", "Empresa removida com sucesso!");
		} catch (DataIntegrityViolationException e) {
			// Agora o erro é específico de integridade do banco
			attributes.addFlashAttribute("mensagemErro",
					"Não é possível excluir esta empresa. Ainda existem registros (como usuários, contatos ou grupos) vinculados a ela.");
		} catch (Exception e) {
			// Para outros erros não previstos
			attributes.addFlashAttribute("mensagemErro", "Ocorreu um erro inesperado ao tentar excluir a empresa.");
		}
		return new ModelAndView("redirect:/administrativo/empresas");
	}
}