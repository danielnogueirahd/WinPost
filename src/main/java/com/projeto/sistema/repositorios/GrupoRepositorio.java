package com.projeto.sistema.repositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.projeto.sistema.modelos.Empresa; // <-- IMPORT NOVO
import com.projeto.sistema.modelos.Grupo;

public interface GrupoRepositorio extends JpaRepository<Grupo, Long> {
    
    // --- NOVA BUSCA BÁSICA (Substitui o findAll) ---
    List<Grupo> findByEmpresa(Empresa empresa);

    // 1. Usado na tela "Gerenciar Grupos" (Ordenado de A a Z + BLINDADO POR EMPRESA)
    List<Grupo> findByNomeContainingIgnoreCaseAndEmpresaOrderByNomeAsc(String nome, Empresa empresa);

    // 2. Usado no "EmailService" (Busca simples para o robô de envio + BLINDADO POR EMPRESA)
    List<Grupo> findByNomeContainingIgnoreCaseAndEmpresa(String nome, Empresa empresa);
}