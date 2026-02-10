package com.projeto.sistema.repositorios;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // <--- Importante
import com.projeto.sistema.modelos.Lembrete;

@Repository // <--- Adicione esta anotação para garantir que o Spring o encontre
public interface LembreteRepositorio extends JpaRepository<Lembrete, Long> {
    List<Lembrete> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);
}