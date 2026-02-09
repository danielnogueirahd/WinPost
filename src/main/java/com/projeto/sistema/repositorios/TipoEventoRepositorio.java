package com.projeto.sistema.repositorios;

import com.projeto.sistema.modelos.TipoEvento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoEventoRepositorio extends JpaRepository<TipoEvento, Long> {
    // Se for por usuário: List<TipoEvento> findByUsuario(Usuario usuario);
}