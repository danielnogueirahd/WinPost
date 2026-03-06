package com.projeto.sistema.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.projeto.sistema.modelos.Contatos;

@Repository
public interface ContatosRepositorio extends JpaRepository<Contatos, Long> {

    // Busca os 10 últimos cadastrados (para a Home)
    List<Contatos> findTop10ByOrderByIdDesc();

    // Busca por Grupo específico
    @Query("SELECT c FROM Contatos c JOIN c.grupos g WHERE g.id = :grupoId")
    List<Contatos> findByGrupoId(@Param("grupoId") Long grupoId);

    // Busca aniversariantes pelo mês (Mantém busca geral por mês)
    @Query("SELECT c FROM Contatos c WHERE c.dataNascimento LIKE %:mesFormatado")
    List<Contatos> findByMesAniversario(@Param("mesFormatado") String mesFormatado);

    // --- CORREÇÃO DO ERRO E FILTRO DO SWITCH ---
    // 1. Voltamos para 1 argumento (String diaMesFormatado) para o Java não reclamar.
    // 2. Adicionamos "AND c.exibirNaAgenda = true" para respeitar a sua nova opção.
    @Query("SELECT c FROM Contatos c WHERE c.dataNascimento = :diaMesFormatado AND c.exibirNaAgenda = true")
    List<Contatos> findByDiaEMesAniversario(@Param("diaMesFormatado") String diaMesFormatado);
    
    // Filtro completo para relatórios
    @Query("SELECT DISTINCT c FROM Contatos c LEFT JOIN c.grupos g WHERE "
            + "(:nome IS NULL OR lower(c.nome) LIKE lower(concat('%', :nome, '%'))) AND "
            + "(:cidade IS NULL OR lower(c.cidade) LIKE lower(concat('%', :cidade, '%'))) AND "
            + "(:estado IS NULL OR c.estado = :estado) AND " 
            + "(:grupoId IS NULL OR g.id = :grupoId)")
    List<Contatos> filtrarRelatorio(@Param("nome") String nome, 
                                    @Param("cidade") String cidade,
                                    @Param("estado") String estado, 
                                    @Param("grupoId") Long grupoId);
    
    // Filtro para a tela de listagem
    @Query("SELECT DISTINCT c FROM Contatos c LEFT JOIN c.grupos g WHERE " +
           "(:nome IS NULL OR :nome = '' OR lower(c.nome) LIKE lower(concat('%', :nome, '%'))) AND " +
           "(:cidade IS NULL OR :cidade = '' OR lower(c.cidade) LIKE lower(concat('%', :cidade, '%'))) AND " +
           "(:grupoId IS NULL OR g.id = :grupoId)")
    List<Contatos> filtrarBusca(@Param("nome") String nome, 
                                @Param("cidade") String cidade, 
                                @Param("grupoId") Long grupoId);
                                
    // Validação extra para evitar emails duplicados (opcional, mas recomendado)
    boolean existsByEmail(String email);
}