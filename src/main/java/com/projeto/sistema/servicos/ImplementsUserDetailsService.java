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
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// 1. Busca o usuário no banco pelo username
		Usuario usuario = usuarioRepositorio.findByUsername(username);

		if (usuario == null) {
			throw new UsernameNotFoundException("Usuário não encontrado!");
		}

		// 2. Traduz as permissões do seu Perfil para os "Crachás" do Spring Security
		List<GrantedAuthority> autoridades = new ArrayList<>();
		
		if (usuario.getPerfil() != null && usuario.getPerfil().getPermissoes() != null) {
			for (Permissao permissao : usuario.getPerfil().getPermissoes()) {
				autoridades.add(new SimpleGrantedAuthority(permissao.name()));
			}
		}

		// 3. Retorna o nosso UsuarioLogado (Crachá VIP) numa única linha para evitar erros
		// Retorna o nosso UsuarioLogado adicionando o usuario.getNome() no final
		return new UsuarioLogado(usuario.getUsername(), usuario.getSenha(), autoridades, usuario.getEmpresa(), usuario.getId(), usuario.getNome());
	}
}