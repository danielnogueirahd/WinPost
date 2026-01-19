package com.projeto.sistema.repositorios;

import java.util.List; // Não esqueça deste import
import org.springframework.data.jpa.repository.JpaRepository;
import com.projeto.sistema.modelos.Grupo;

public interface GrupoRepositorio extends JpaRepository<Grupo, Long> {
    
    // Busca grupos que contenham o texto pesquisado (ignorando maiúsculas/minúsculas)
    List<Grupo> findByNomeContainingIgnoreCase(String nome);
}