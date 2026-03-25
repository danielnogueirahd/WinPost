package com.projeto.sistema.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.projeto.sistema.modelos.Perfil;

@Repository
public interface PerfilRepositorio extends JpaRepository<Perfil, Long> {
    Perfil findByNome(String nome);
}