package com.projeto.sistema.servicos;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.sistema.modelos.Permissao;
import com.projeto.sistema.modelos.Usuario;
import com.projeto.sistema.modelos.UsuarioLogado;
import com.projeto.sistema.repositorios.UsuarioRepositorio;

@Service
@Transactional
public class ImplementsUserDetailsService implements UserDetailsService {

	@Autowired
	private UsuarioRepositorio usuarioRepositorio;

	@Override
	public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
		// Agora passamos o que a pessoa digitou para os dois parâmetros (Username OU
		// Email)
		Usuario usuario = usuarioRepositorio.findByUsernameOrEmail(login, login);

		if (usuario == null) {
			throw new UsernameNotFoundException("Usuário não encontrado!");
		}

		// Traduz as permissões do seu Perfil para os "Crachás" do Spring Security
		List<GrantedAuthority> autoridades = new ArrayList<>();

		if (usuario.getPerfil() != null && usuario.getPerfil().getPermissoes() != null) {
			for (Permissao permissao : usuario.getPerfil().getPermissoes()) {
				autoridades.add(new SimpleGrantedAuthority(permissao.name()));
			}
		}

		// Retorna o nosso UsuarioLogado
		return new UsuarioLogado(usuario.getUsername(), usuario.getSenha(), autoridades, usuario.getEmpresa(),
				usuario.getId(), usuario.getNome());
	}
}