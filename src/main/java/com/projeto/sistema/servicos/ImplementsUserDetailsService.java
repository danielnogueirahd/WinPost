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
    public UserDetails loadUserByUsername(String emailLogin) throws UsernameNotFoundException {
        
        // CORREÇÃO: Busca apenas pelo campo EMAIL do usuário
        Usuario usuario = usuarioRepositorio.findByEmail(emailLogin);

        if (usuario == null) {
            throw new UsernameNotFoundException("E-mail não encontrado ou inválido!");
        }

        List<GrantedAuthority> autoridades = new ArrayList<>();

        if (usuario.getPerfil() != null && usuario.getPerfil().getPermissoes() != null) {
            for (Permissao permissao : usuario.getPerfil().getPermissoes()) {
                autoridades.add(new SimpleGrantedAuthority(permissao.name()));
            }
        }

        // Super Admin: usuário sem empresa vinculada (Admin Master) OU com permissão CONFIGURACOES_SISTEMA
        boolean isSuperAdmin = (usuario.getEmpresa() == null) ||
                autoridades.stream().anyMatch(a -> a.getAuthority().equals("CONFIGURACOES_SISTEMA"));

        return new UsuarioLogado(
                usuario.getUsername(), // Mantém o username original apenas para referência interna (se necessário)
                usuario.getSenha(),
                autoridades,
                usuario.getEmpresa(),
                usuario.getId(),
                usuario.getNome(),
                isSuperAdmin
        );
    }
}