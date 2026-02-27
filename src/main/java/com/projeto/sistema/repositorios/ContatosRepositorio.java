package com.projeto.sistema.repositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.projeto.sistema.modelos.Contatos;

@Repository
public interface ContatosRepositorio extends JpaRepository<Contatos, Long> {

    List<Contatos> findTop10ByOrderByIdDesc();

    @Query("SELECT c FROM Contatos c JOIN c.grupos g WHERE g.id = :grupoId")
    List<Contatos> findByGrupoId(@Param("grupoId") Long grupoId);

    @Query("SELECT c FROM Contatos c WHERE c.dataNascimento LIKE %:mesFormatado")
    List<Contatos> findByMesAniversario(@Param("mesFormatado") String mesFormatado);

    @Query("SELECT c FROM Contatos c WHERE c.dataNascimento = :diaMesFormatado")
    List<Contatos> findByDiaEMesAniversario(@Param("diaMesFormatado") String diaMesFormatado);
    
    @Query("SELECT DISTINCT c FROM Contatos c LEFT JOIN c.grupos g WHERE "
            + "(:nome IS NULL OR lower(c.nome) LIKE lower(concat('%', :nome, '%'))) AND "
            + "(:cidade IS NULL OR lower(c.cidade) LIKE lower(concat('%', :cidade, '%'))) AND "
            + "(:estado IS NULL OR c.estado = :estado) AND " 
            + "(:grupoId IS NULL OR g.id = :grupoId)")
    List<Contatos> filtrarRelatorio(@Param("nome") String nome, 
                                    @Param("cidade") String cidade,
                                    @Param("estado") String estado, 
                                    @Param("grupoId") Long grupoId);
    
    @Query("SELECT DISTINCT c FROM Contatos c LEFT JOIN c.grupos g WHERE " +
           "(:nome IS NULL OR :nome = '' OR lower(c.nome) LIKE lower(concat('%', :nome, '%'))) AND " +
           "(:cidade IS NULL OR :cidade = '' OR lower(c.cidade) LIKE lower(concat('%', :cidade, '%'))) AND " +
           "(:grupoId IS NULL OR g.id = :grupoId)")
    List<Contatos> filtrarBusca(@Param("nome") String nome, 
                                @Param("cidade") String cidade, 
                                @Param("grupoId") Long grupoId);
}