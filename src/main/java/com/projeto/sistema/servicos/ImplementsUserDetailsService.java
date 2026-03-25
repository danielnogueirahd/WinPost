package com.projeto.sistema.servicos;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projeto.sistema.modelos.Permissao;
import com.projeto.sistema.modelos.Usuario;
import com.projeto.sistema.repositorios.UsuarioRepositorio;

@Service
@Transactional // Muito importante: garante que a lista de permissões seja lida corretamente do banco
public class ImplementsUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepositorio usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username);

        if (usuario == null) {
            throw new UsernameNotFoundException("Usuário não encontrado!");
        }

        // A MÁGICA ACONTECE AQUI: Criamos a "mochila" de permissões do Spring
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        // Se o usuário tem um perfil...
        if (usuario.getPerfil() != null) {
            
            // 1. Adicionamos o nome do perfil dele como uma ROLE (Ex: ROLE_MASTER)
            authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().getNome()));
            
            // 2. Abrimos o Perfil, pegamos cada uma das Permissões do Enum e colocamos na mochila
            for (Permissao permissao : usuario.getPerfil().getPermissoes()) {
                authorities.add(new SimpleGrantedAuthority(permissao.name()));
            }
        }

        // O Spring Security pega essa mochila e deixa o usuário entrar na festa
        return new User(
            usuario.getUsername(), 
            usuario.getSenha(), 
            true, true, true, true, 
            authorities
        );
    }
}