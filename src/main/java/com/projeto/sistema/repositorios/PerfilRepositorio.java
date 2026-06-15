package com.projeto.sistema.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.projeto.sistema.modelos.Empresa;
import com.projeto.sistema.modelos.Perfil;

@Repository
public interface PerfilRepositorio extends JpaRepository<Perfil, Long> {

    Perfil findByNome(String nome);

    // Perfis da empresa + perfis globais (empresa nula = perfis do sistema)
    @Query("SELECT p FROM Perfil p WHERE p.empresa = :empresa OR p.empresa IS NULL ORDER BY p.nome")
    List<Perfil> findByEmpresaOrGlobal(@Param("empresa") Empresa empresa);

    // Apenas perfis da empresa (Tenant Admin gerencia apenas os seus)
    List<Perfil> findByEmpresa(Empresa empresa);

    // Super Admin: todos os perfis
    List<Perfil> findAllByOrderByNomeAsc();
}