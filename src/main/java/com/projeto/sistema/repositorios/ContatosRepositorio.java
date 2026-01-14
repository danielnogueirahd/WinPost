package com.projeto.sistema.repositorios;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.projeto.sistema.modelos.Contatos;

public interface ContatosRepositorio extends JpaRepository<Contatos, Long> {

    List<Contatos> findTop10ByOrderByIdDesc();

    @Query("SELECT c FROM Contatos c JOIN c.grupos g WHERE g.id = :grupoId")
    List<Contatos> findByGrupoId(@Param("grupoId") Long grupoId);
    
    // --- NOVO CÓDIGO (Passo 1) ---
    // Busca aniversariantes pelo mês (1 = Janeiro, 2 = Fevereiro, etc.)
    @Query("SELECT c FROM Contatos c WHERE MONTH(c.dataNascimento) = :mes")
    List<Contatos> findByMesAniversario(@Param("mes") Integer mes);
    // -----------------------------

    @Query("SELECT DISTINCT c FROM Contatos c LEFT JOIN c.grupos g WHERE " +
           "(:nome IS NULL OR lower(c.nome) LIKE lower(concat('%', :nome, '%'))) AND " +
           "(:cidade IS NULL OR lower(c.cidade) LIKE lower(concat('%', :cidade, '%'))) AND " +
           "(:estado IS NULL OR c.estado = :estado) AND " +
           "(:grupoId IS NULL OR g.id = :grupoId) AND " + 
           "(:dataInicio IS NULL OR c.dataNascimento >= :dataInicio) AND " +
           "(:dataFim IS NULL OR c.dataNascimento <= :dataFim)")
    List<Contatos> filtrarRelatorio(
            @Param("nome") String nome, 
            @Param("cidade") String cidade,
            @Param("estado") String estado,
            @Param("grupoId") Long grupoId, 
            @Param("dataInicio") LocalDate dataInicio, 
            @Param("dataFim") LocalDate dataFim);
}