package com.projeto.sistema.repositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.projeto.sistema.modelos.EventoAgenda;
import com.projeto.sistema.modelos.Empresa;

public interface EventoAgendaRepositorio extends JpaRepository<EventoAgenda, Long> {
    
    // Ajuste o filtro de data conforme o formato que você está usando no banco (ex: month/year)
    @Query("SELECT e FROM EventoAgenda e WHERE e.empresa = :empresa")
    List<EventoAgenda> findByEmpresa(@Param("empresa") Empresa empresa);
}