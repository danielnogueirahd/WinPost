package com.projeto.sistema.repositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.projeto.sistema.modelos.Grupo;

public interface GrupoRepositorio extends JpaRepository<Grupo, Long> {
    
    // 1. Usado na tela "Gerenciar Grupos" (Ordenado de A a Z)
    List<Grupo> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);

    // 2. Usado no "EmailService" (Busca simples para o robô de envio)
    // Adicione esta linha abaixo para corrigir o erro:
    List<Grupo> findByNomeContainingIgnoreCase(String nome);
}