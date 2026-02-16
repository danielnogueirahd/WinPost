package com.projeto.sistema.repositorios;

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

    // MUDANÇA: Busca aniversariantes pelo mês usando texto (Ex: "/08")
    @Query("SELECT c FROM Contatos c WHERE c.dataNascimento LIKE %:mesFormatado")
    List<Contatos> findByMesAniversario(@Param("mesFormatado") String mesFormatado);

    // MUDANÇA: Busca aniversariantes de dia e mês específicos usando texto exato (Ex: "15/08")
    @Query("SELECT c FROM Contatos c WHERE c.dataNascimento = :diaMesFormatado")
    List<Contatos> findByDiaEMesAniversario(@Param("diaMesFormatado") String diaMesFormatado);

    // MUDANÇA: O filtro de Relatórios teve os parâmetros de Data removidos.
    // Motivo: O banco não consegue fazer comparações de "maior ou igual" num texto "DD/MM".
    @Query("SELECT DISTINCT c FROM Contatos c LEFT JOIN c.grupos g WHERE "
            + "(:nome IS NULL OR lower(c.nome) LIKE lower(concat('%', :nome, '%'))) AND "
            + "(:cidade IS NULL OR lower(c.cidade) LIKE lower(concat('%', :cidade, '%'))) AND "
            + "(:estado IS NULL OR c.estado = :estado) AND " + "(:grupoId IS NULL OR g.id = :grupoId)")
    List<Contatos> filtrarRelatorio(@Param("nome") String nome, @Param("cidade") String cidade,
            @Param("estado") String estado, @Param("grupoId") Long grupoId);

    // O método findByAniversarioNoPeriodo foi removido pois não é possível 
    // buscar intervalos (BETWEEN) diretamente no banco com uma String de dia/mês.

    // MUDANÇA: Busca aniversariantes num dia específico por texto
    @Query("SELECT c FROM Contatos c WHERE c.dataNascimento = :diaMesFormatado")
    List<Contatos> findByAniversarioNoDia(@Param("diaMesFormatado") String diaMesFormatado);

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