package com.projeto.sistema.repositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.projeto.sistema.modelos.Empresa;

@Repository
public interface EmpresaRepositorio extends JpaRepository<Empresa, Long> {
    
    // O Spring Data cria a query SQL automaticamente: SELECT * FROM empresas WHERE ativo = true
    List<Empresa> findByAtivoTrue();
    
}