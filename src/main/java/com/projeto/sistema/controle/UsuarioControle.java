package com.projeto.sistema.controle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.projeto.sistema.modelos.Usuario;
import com.projeto.sistema.repositorios.PerfilRepositorio;
import com.projeto.sistema.repositorios.UsuarioRepositorio;
import com.projeto.sistema.servicos.EmpresaService;

@Controller
@RequestMapping("/administrativo/usuarios")
public class UsuarioControle {

	@Autowired
	private UsuarioRepositorio usuarioRepositorio;
	
	

	@Autowired
	private PerfilRepositorio perfilRepositorio;

	@Autowired
	private EmpresaService empresaService;

	// A NOSSA MÁQUINA DE CRIPTOGRAFAR SENHAS
	@Autowired
	private PasswordEncoder passwordEncoder;

	// FECHADURA: Apenas quem tem permissão para VISUALIZAR
	@PreAuthorize("hasAuthority('USUARIO_VISUALIZAR')")
	@GetMapping
	public ModelAndView listarUsuarios() {
		ModelAndView mv = new ModelAndView("administrativo/usuarios/lista");
		mv.addObject("listaUsuarios", usuarioRepositorio.findAll());
		mv.addObject("paginaAtiva", "usuarios");
		return mv;
	}

	@PreAuthorize("hasAuthority('USUARIO_CRIAR')")
	@GetMapping("/novo")
	public ModelAndView novoUsuario() {
	    ModelAndView mv = new ModelAndView("administrativo/usuarios/cadastro");
	    mv.addObject("usuario", new Usuario());
	    mv.addObject("listaPerfis", perfilRepositorio.findAll());
	    
	    // Esta linha é obrigatória para o dropdown de empresas aparecer!
	    mv.addObject("listaEmpresas", empresaService.listarTodas()); 
	    
	    mv.addObject("paginaAtiva", "usuarios");
	    return mv;
	}

	// FECHADURA: Tanto quem CRIA quanto quem EDITA pode salvar
	@PreAuthorize("hasAnyAuthority('USUARIO_CRIAR', 'USUARIO_EDITAR')")
	@PostMapping("/salvar")
	public String salvarUsuario(Usuario usuario, RedirectAttributes attributes) {

		// Se o ID for null, é um NOVO usuário
		if (usuario.getId() == null) {
			String senhaCriptografada = passwordEncoder.encode(usuario.getSenha());
			usuario.setSenha(senhaCriptografada);
		} else {
			// Se já tem ID, é uma EDIÇÃO! Busca o usuário antigo no banco
			Usuario usuarioAntigo = usuarioRepositorio.findById(usuario.getId()).orElse(null);

			if (usuarioAntigo != null) {
				// Se você digitou uma senha nova na tela, ele criptografa a nova
				if (usuario.getSenha() != null && !usuario.getSenha().trim().isEmpty()) {
					usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
				} else {
					// Se você deixou a senha em branco na tela, ele mantém a senha antiga que
					// estava no banco
					usuario.setSenha(usuarioAntigo.getSenha());
				}
			}
		}

		usuarioRepositorio.save(usuario);
		return "redirect:/administrativo/usuarios";
	}

	@PreAuthorize("hasAuthority('USUARIO_EDITAR')")
	@GetMapping("/editar/{id}")
	public ModelAndView editarUsuario(@PathVariable("id") Long id) {
		ModelAndView mv = new ModelAndView("administrativo/usuarios/cadastro");

		mv.addObject("usuario", usuarioRepositorio.findById(id).orElse(new Usuario()));
		mv.addObject("listaPerfis", perfilRepositorio.findAll());

		// ADICIONE ESTA LINHA TAMBÉM:
		mv.addObject("listaEmpresas", empresaService.listarTodas());

		mv.addObject("paginaAtiva", "usuarios");

		return mv;
	}

	// FECHADURA: Apenas quem tem permissão para EXCLUIR
	@PreAuthorize("hasAuthority('USUARIO_EXCLUIR')")
	@GetMapping("/remover/{id}")
	public String removerUsuario(@PathVariable("id") Long id) {
		usuarioRepositorio.deleteById(id);
		return "redirect:/administrativo/usuarios";
	}
}