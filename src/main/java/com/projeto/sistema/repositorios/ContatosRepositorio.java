package com.projeto.sistema.repositorios;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.projeto.sistema.modelos.Contatos;

public interface ContatosRepositorio extends JpaRepository<Contatos, Long> {

    // Busca os 10 últimos cadastrados (para a Home)
    List<Contatos> findTop10ByOrderByIdDesc();

    // Busca por Grupo específico
    @Query("SELECT c FROM Contatos c JOIN c.grupos g WHERE g.id = :grupoId")
    List<Contatos> findByGrupoId(@Param("grupoId") Long grupoId);

    // Busca aniversariantes pelo mês
    @Query("SELECT c FROM Contatos c WHERE MONTH(c.dataNascimento) = :mes")
    List<Contatos> findByMesAniversario(@Param("mes") Integer mes);

    // Busca aniversariantes de dia e mês específicos
    @Query("SELECT c FROM Contatos c WHERE DAY(c.dataNascimento) = :dia AND MONTH(c.dataNascimento) = :mes")
    List<Contatos> findByDiaEMesAniversario(@Param("dia") int dia, @Param("mes") int mes);

    // Filtro completo para Relatórios (já existente)
    @Query("SELECT DISTINCT c FROM Contatos c LEFT JOIN c.grupos g WHERE "
            + "(:nome IS NULL OR lower(c.nome) LIKE lower(concat('%', :nome, '%'))) AND "
            + "(:cidade IS NULL OR lower(c.cidade) LIKE lower(concat('%', :cidade, '%'))) AND "
            + "(:estado IS NULL OR c.estado = :estado) AND " + "(:grupoId IS NULL OR g.id = :grupoId) AND "
            + "(:dataInicio IS NULL OR c.dataNascimento >= :dataInicio) AND "
            + "(:dataFim IS NULL OR c.dataNascimento <= :dataFim)")
    List<Contatos> filtrarRelatorio(@Param("nome") String nome, @Param("cidade") String cidade,
            @Param("estado") String estado, @Param("grupoId") Long grupoId, @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim);

    // Busca aniversariantes num período
    @Query("SELECT c FROM Contatos c WHERE (MONTH(c.dataNascimento) * 100 + DAY(c.dataNascimento)) "
            + "BETWEEN (MONTH(:dataInicio) * 100 + DAY(:dataInicio)) " + "AND (MONTH(:dataFim) * 100 + DAY(:dataFim))")
    List<Contatos> findByAniversarioNoPeriodo(@Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim);

    @Query("SELECT c FROM Contatos c WHERE DAY(c.dataNascimento) = :dia AND MONTH(c.dataNascimento) = :mes")
    List<Contatos> findByAniversarioNoDia(@Param("dia") int dia, @Param("mes") int mes);

    // =========================================================================
    //  NOVO MÉTODO PARA A PESQUISA DA TELA (ADICIONADO AQUI NO FINAL)
    // =========================================================================
    @Query("SELECT DISTINCT c FROM Contatos c LEFT JOIN c.grupos g WHERE " +
           "(:nome IS NULL OR :nome = '' OR lower(c.nome) LIKE lower(concat('%', :nome, '%'))) AND " +
           "(:cidade IS NULL OR :cidade = '' OR lower(c.cidade) LIKE lower(concat('%', :cidade, '%'))) AND " +
           "(:grupoId IS NULL OR g.id = :grupoId)")
    List<Contatos> filtrarBusca(
            @Param("nome") String nome, 
            @Param("cidade") String cidade, 
            @Param("grupoId") Long grupoId);

}