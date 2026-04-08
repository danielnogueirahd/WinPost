package com.projeto.sistema.repositorios;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; 

import com.projeto.sistema.modelos.Empresa; // <-- NOVO IMPORT
import com.projeto.sistema.modelos.Lembrete;

@Repository 
public interface LembreteRepositorio extends JpaRepository<Lembrete, Long> {
    
    // <-- BLINDADO COM "AndEmpresa"
    List<Lembrete> findByDataHoraBetweenAndEmpresa(LocalDateTime inicio, LocalDateTime fim, Empresa empresa);
}