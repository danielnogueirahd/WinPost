package com.projeto.sistema.repositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.projeto.sistema.modelos.Empresa;
import com.projeto.sistema.modelos.Grupo;

public interface GrupoRepositorio extends JpaRepository<Grupo, Long> {
    
    // 1. Busca todos os grupos da empresa
    List<Grupo> findByEmpresa(Empresa empresa);
    
    // 2. Usado na tela de gerenciar para a barra de pesquisa
    List<Grupo> findByNomeContainingIgnoreCaseAndEmpresaOrderByNomeAsc(String pesquisa, Empresa empresa);

    // 3. O MÉTODO QUE ESTAVA A FALTAR PARA O ROBÔ DO EMAIL SERVICE!
    List<Grupo> findByNomeContainingIgnoreCaseAndEmpresa(String nome, Empresa empresa);
}