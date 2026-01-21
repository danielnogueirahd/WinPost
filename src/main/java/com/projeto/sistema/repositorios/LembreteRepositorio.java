package com.projeto.sistema.repositorios;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.projeto.sistema.modelos.Lembrete;

public interface LembreteRepositorio extends JpaRepository<Lembrete, Long> {
    List<Lembrete> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);
}