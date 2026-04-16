package com.projeto.sistema.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.projeto.sistema.modelos.Contatos;
import com.projeto.sistema.modelos.Empresa; // IMPORT NOVO DA EMPRESA AQUI!

@Repository
public interface ContatosRepositorio extends JpaRepository<Contatos, Long> {

	// --- NOVA BUSCA BÁSICA ---
	// Substitui o findAll() padrão do sistema
	List<Contatos> findByEmpresa(Empresa empresa);

	// --- HOME ---
	// Busca os 10 últimos cadastrados DA EMPRESA (para a Home)
	List<Contatos> findTop10ByEmpresaOrderByIdDesc(Empresa empresa);

	// --- BUSCA POR GRUPO ---
	// Adicionamos a checagem da empresa para garantir isolamento
	@Query("SELECT c FROM Contatos c JOIN c.grupos g WHERE g.id = :grupoId AND c.empresa = :empresa")
	List<Contatos> findByGrupoId(@Param("grupoId") Long grupoId, @Param("empresa") Empresa empresa);

	// --- ANIVERSARIANTES ---
	// Busca aniversariantes pelo mês, garantindo que são da empresa do utilizador
	@Query("SELECT c FROM Contatos c WHERE c.dataNascimento LIKE %:mesFormatado AND c.empresa = :empresa")
	List<Contatos> findByMesAniversario(@Param("mesFormatado") String mesFormatado, @Param("empresa") Empresa empresa);

	// Busca aniversariantes do dia exibidos na agenda (Filtro Switch + Empresa)
	@Query("SELECT c FROM Contatos c WHERE c.dataNascimento = :diaMesFormatado AND c.exibirNaAgenda = true AND c.empresa = :empresa")
	List<Contatos> findByDiaEMesAniversario(@Param("diaMesFormatado") String diaMesFormatado,
			@Param("empresa") Empresa empresa);

	// --- RELATÓRIOS ---
	@Query("SELECT DISTINCT c FROM Contatos c LEFT JOIN c.grupos g WHERE " + "c.empresa = :empresa AND "
			+ "(:nome IS NULL OR lower(c.nome) LIKE lower(concat('%', :nome, '%'))) AND "
			+ "(:cidade IS NULL OR lower(c.cidade) LIKE lower(concat('%', :cidade, '%'))) AND "
			+ "(:estado IS NULL OR c.estado = :estado) AND " + "(:grupoId IS NULL OR :grupoId = 0 OR g.id = :grupoId)") // <--
																														// CORREÇÃO
																														// AQUI
	List<Contatos> filtrarRelatorio(@Param("nome") String nome, @Param("cidade") String cidade,
			@Param("estado") String estado, @Param("grupoId") Long grupoId, @Param("empresa") Empresa empresa);

	// --- LISTAGEM / BUSCA NA TELA ---
	@Query("SELECT DISTINCT c FROM Contatos c LEFT JOIN c.grupos g WHERE " + "c.empresa = :empresa AND "
			+ "(:nome IS NULL OR :nome = '' OR lower(c.nome) LIKE lower(concat('%', :nome, '%'))) AND "
			+ "(:cidade IS NULL OR :cidade = '' OR lower(c.cidade) LIKE lower(concat('%', :cidade, '%'))) AND "
			+ "(:grupoId IS NULL OR :grupoId = 0 OR g.id = :grupoId)") // <-- CORREÇÃO AQUI
	List<Contatos> filtrarBusca(@Param("nome") String nome, @Param("cidade") String cidade,
			@Param("grupoId") Long grupoId, @Param("empresa") Empresa empresa);

	// Validação extra para evitar emails duplicados DENTRO DA MESMA EMPRESA
	// (O João pode existir na Empresa A e outro João com o mesmo email na Empresa
	// B)
	boolean existsByEmailAndEmpresa(String email, Empresa empresa);
}