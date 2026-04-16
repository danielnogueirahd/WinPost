package com.projeto.sistema.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import com.projeto.sistema.modelos.Empresa;

public interface EmpresaRepositorio extends JpaRepository<Empresa, Long> {
}