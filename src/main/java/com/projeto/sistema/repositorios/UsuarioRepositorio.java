package com.projeto.sistema.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import com.projeto.sistema.modelos.Usuario;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    
    Usuario findByUsername(String username);
    
    // NOVO: Busca o usuário tanto pelo username quanto pelo e-mail
    Usuario findByUsernameOrEmail(String username, String email);
}