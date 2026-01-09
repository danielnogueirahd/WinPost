package com.projeto.sistema.repositorios;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.projeto.sistema.modelos.Contatos;

public interface ContatosRepositorio extends JpaRepository<Contatos, Long> {

    // 1. Método existente (usado nos cards do Dashboard)
	List<Contatos> findTop10ByOrderByIdDesc();

    // 2. NOVO MÉTODO: Filtro Inteligente para o Relatório
    // A lógica ":parametro IS NULL" faz com que o banco ignore o filtro se ele vier vazio.
    @Query("SELECT c FROM Contatos c WHERE " +
           "(:nome IS NULL OR lower(c.nome) LIKE lower(concat('%', :nome, '%'))) AND " +
           "(:cidade IS NULL OR lower(c.cidade) LIKE lower(concat('%', :cidade, '%'))) AND " +
           "(:estado IS NULL OR c.estado = :estado) AND " +
           "(:dataInicio IS NULL OR c.dataCadastro >= :dataInicio) AND " +
           "(:dataFim IS NULL OR c.dataCadastro <= :dataFim)")
    List<Contatos> filtrarRelatorio(
            @Param("nome") String nome, 
            @Param("cidade") String cidade,
            @Param("estado") String estado,
            @Param("dataInicio") LocalDate dataInicio, 
            @Param("dataFim") LocalDate dataFim);
}