package com.projeto.sistema.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.projeto.sistema.modelos.Empresa;
import com.projeto.sistema.modelos.Usuario;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    Usuario findByUsername(String username);

    // Login por username ou email
    Usuario findByUsernameOrEmail(String username, String email);

    // Usuários da empresa (Tenant Admin vê apenas os seus)
    List<Usuario> findByEmpresa(Empresa empresa);

    // Atalho por ID de empresa
    List<Usuario> findByEmpresaId(Long empresaId);

    // Super Admin: todos os usuários
    List<Usuario> findAllByOrderByNomeAsc();

    // Verifica duplicidade de username na mesma empresa
    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.username = :username AND u.empresa = :empresa AND (:id IS NULL OR u.id <> :id)")
    boolean existsByUsernameAndEmpresaAndIdNot(@Param("username") String username,
            @Param("empresa") Empresa empresa, @Param("id") Long id);

    // Verifica duplicidade de email na mesma empresa
    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.email = :email AND u.empresa = :empresa AND (:id IS NULL OR u.id <> :id)")
    boolean existsByEmailAndEmpresaAndIdNot(@Param("email") String email,
            @Param("empresa") Empresa empresa, @Param("id") Long id);
}