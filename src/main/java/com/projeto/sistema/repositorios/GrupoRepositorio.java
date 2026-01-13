package com.projeto.sistema.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import com.projeto.sistema.modelos.Grupo;

public interface GrupoRepositorio extends JpaRepository<Grupo, Long> {
    // Aqui poderemos adicionar métodos customizados no futuro, como:
    // Grupo findByNome(String nome);
}