package com.projeto.sistema.repositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.projeto.sistema.modelos.Contatos;
import com.projeto.sistema.modelos.Empresa;

@Repository
public interface ContatosRepositorio extends JpaRepository<Contatos, Long> {

    // --- BUSCA BÁSICA POR EMPRESA ---
    List<Contatos> findByEmpresa(Empresa empresa);

    // --- HOME ---
    List<Contatos> findTop10ByEmpresaOrderByIdDesc(Empresa empresa);

    // --- BUSCA POR GRUPO (filtrada por empresa) ---
    @Query("SELECT c FROM Contatos c JOIN c.grupos g WHERE g.id = :grupoId AND c.empresa = :empresa")
    List<Contatos> findByGrupoId(@Param("grupoId") Long grupoId, @Param("empresa") Empresa empresa);

    // --- ANIVERSARIANTES POR MÊS (filtrado por empresa) ---
    @Query("SELECT c FROM Contatos c WHERE c.dataNascimento LIKE %:mesFormatado AND c.empresa = :empresa")
    List<Contatos> findByMesAniversario(@Param("mesFormatado") String mesFormatado, @Param("empresa") Empresa empresa);

    // --- ANIVERSARIANTES DO DIA (exibir na agenda, filtrado por empresa) ---
    @Query("SELECT c FROM Contatos c WHERE c.dataNascimento = :diaMesFormatado AND c.exibirNaAgenda = true AND c.empresa = :empresa")
    List<Contatos> findByDiaEMesAniversario(@Param("diaMesFormatado") String diaMesFormatado,
            @Param("empresa") Empresa empresa);

    // --- RELATÓRIOS (filtrado por empresa) ---
    @Query("SELECT DISTINCT c FROM Contatos c LEFT JOIN c.grupos g WHERE "
            + "c.empresa = :empresa AND "
            + "(:nome IS NULL OR lower(c.nome) LIKE lower(concat('%', :nome, '%'))) AND "
            + "(:cidade IS NULL OR lower(c.cidade) LIKE lower(concat('%', :cidade, '%'))) AND "
            + "(:estado IS NULL OR c.estado = :estado) AND "
            + "(:grupoId IS NULL OR :grupoId = 0 OR g.id = :grupoId)")
    List<Contatos> filtrarRelatorio(@Param("nome") String nome, @Param("cidade") String cidade,
            @Param("estado") String estado, @Param("grupoId") Long grupoId, @Param("empresa") Empresa empresa);

    // --- LISTAGEM / BUSCA NA TELA (filtrado por empresa) ---
    @Query("SELECT DISTINCT c FROM Contatos c LEFT JOIN c.grupos g WHERE "
            + "c.empresa = :empresa AND "
            + "(:nome IS NULL OR :nome = '' OR lower(c.nome) LIKE lower(concat('%', :nome, '%'))) AND "
            + "(:cidade IS NULL OR :cidade = '' OR lower(c.cidade) LIKE lower(concat('%', :cidade, '%'))) AND "
            + "(:grupoId IS NULL OR :grupoId = 0 OR g.id = :grupoId)")
    List<Contatos> filtrarBusca(@Param("nome") String nome, @Param("cidade") String cidade,
            @Param("grupoId") Long grupoId, @Param("empresa") Empresa empresa);

    // --- SUPER ADMIN: listagem irrestrita com filtros ---
    @Query("SELECT DISTINCT c FROM Contatos c LEFT JOIN c.grupos g WHERE "
            + "(:nome IS NULL OR :nome = '' OR lower(c.nome) LIKE lower(concat('%', :nome, '%'))) AND "
            + "(:cidade IS NULL OR :cidade = '' OR lower(c.cidade) LIKE lower(concat('%', :cidade, '%'))) AND "
            + "(:grupoId IS NULL OR :grupoId = 0 OR g.id = :grupoId) AND "
            + "(:empresaId IS NULL OR c.empresa.id = :empresaId)")
    List<Contatos> filtrarBuscaSuperAdmin(@Param("nome") String nome, @Param("cidade") String cidade,
            @Param("grupoId") Long grupoId, @Param("empresaId") Long empresaId);

    // --- VALIDAÇÃO DE EMAIL ÚNICO DENTRO DA EMPRESA ---
    boolean existsByEmailAndEmpresa(String email, Empresa empresa);

    // --- VALIDAÇÃO DE EMAIL ÚNICO EXCLUINDO O PRÓPRIO CONTATO (edição) ---
    @Query("SELECT COUNT(c) > 0 FROM Contatos c WHERE c.email = :email AND c.empresa = :empresa AND c.id <> :id")
    boolean existsByEmailAndEmpresaAndIdNot(@Param("email") String email, @Param("empresa") Empresa empresa,
            @Param("id") Long id);
}