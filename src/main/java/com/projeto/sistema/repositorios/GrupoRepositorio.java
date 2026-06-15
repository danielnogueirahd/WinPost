package com.projeto.sistema.repositorios;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.projeto.sistema.modelos.Empresa;
import com.projeto.sistema.modelos.Grupo;

public interface GrupoRepositorio extends JpaRepository<Grupo, Long> {

    // Busca grupos da empresa
    List<Grupo> findByEmpresa(Empresa empresa);

    // Busca com filtro de nome para gerenciar (tenant)
    List<Grupo> findByNomeContainingIgnoreCaseAndEmpresaOrderByNomeAsc(String pesquisa, Empresa empresa);

    // Usado pelo EmailService (robô agendado)
    List<Grupo> findByNomeContainingIgnoreCaseAndEmpresa(String nome, Empresa empresa);

    // Super Admin: busca grupo por ID garantindo que é da empresa do usuário (ou sem filtro para SA)
    @Query("SELECT g FROM Grupo g WHERE g.id = :id AND (:empresaId IS NULL OR g.empresa.id = :empresaId)")
    Optional<Grupo> findByIdAndEmpresaOptional(@Param("id") Long id, @Param("empresaId") Long empresaId);
}