package com.projeto.sistema.repositorios;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.projeto.sistema.modelos.Empresa;
import com.projeto.sistema.modelos.MensagemLog;

public interface MensagemLogRepositorio extends JpaRepository<MensagemLog, Long> {

    // --- QUERY PRINCIPAL BLINDADA POR EMPRESA ---
    @Query("SELECT m FROM MensagemLog m WHERE "
            + "m.empresa = :empresa AND "
            + "(:pasta IS NULL OR m.pasta = :pasta) AND "
            + "(:favorito IS NULL OR m.favorito = :favorito) AND "
            + "(:importante IS NULL OR m.importante = :importante) AND "
            + "(:temAnexo IS NULL OR (:temAnexo = true AND m.nomesAnexos IS NOT NULL AND m.nomesAnexos != '')) AND "
            + "(:termo IS NULL OR (lower(m.nomeGrupoDestino) LIKE lower(concat('%', :termo, '%')) OR lower(m.assunto) LIKE lower(concat('%', :termo, '%')))) AND "
            + "(:dataInicio IS NULL OR m.dataEnvio >= :dataInicio) AND "
            + "(:dataFim IS NULL OR m.dataEnvio <= :dataFim)")
    Page<MensagemLog> filtrarMensagens(
            @Param("pasta") String pasta,
            @Param("favorito") Boolean favorito,
            @Param("importante") Boolean importante,
            @Param("temAnexo") Boolean temAnexo,
            @Param("termo") String termo,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            @Param("empresa") Empresa empresa,
            Pageable pageable);

    // --- SUPER ADMIN: query irrestrita ---
    @Query("SELECT m FROM MensagemLog m WHERE "
            + "(:pasta IS NULL OR m.pasta = :pasta) AND "
            + "(:favorito IS NULL OR m.favorito = :favorito) AND "
            + "(:importante IS NULL OR m.importante = :importante) AND "
            + "(:temAnexo IS NULL OR (:temAnexo = true AND m.nomesAnexos IS NOT NULL AND m.nomesAnexos != '')) AND "
            + "(:termo IS NULL OR (lower(m.nomeGrupoDestino) LIKE lower(concat('%', :termo, '%')) OR lower(m.assunto) LIKE lower(concat('%', :termo, '%')))) AND "
            + "(:dataInicio IS NULL OR m.dataEnvio >= :dataInicio) AND "
            + "(:dataFim IS NULL OR m.dataEnvio <= :dataFim)")
    Page<MensagemLog> filtrarMensagensSuperAdmin(
            @Param("pasta") String pasta,
            @Param("favorito") Boolean favorito,
            @Param("importante") Boolean importante,
            @Param("temAnexo") Boolean temAnexo,
            @Param("termo") String termo,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable);

    // --- CONTADORES BLINDADOS POR EMPRESA ---
    long countByPastaAndEmpresa(String pasta, Empresa empresa);
    long countByPastaAndLidaFalseAndEmpresa(String pasta, Empresa empresa);
    long countByFavoritoTrueAndEmpresa(Empresa empresa);
    long countByImportanteTrueAndEmpresa(Empresa empresa);
    long countByLidaFalseAndEmpresa(Empresa empresa);

    // --- MÉTODOS AUXILIARES BLINDADOS ---
    List<MensagemLog> findTop5ByLidaFalseAndEmpresaOrderByDataEnvioDesc(Empresa empresa);
    List<MensagemLog> findByDataEnvioBetweenAndEmpresa(LocalDateTime dataInicio, LocalDateTime dataFim, Empresa empresa);

    // --- ROBÔ DE AGENDAMENTO (sem filtro de empresa - processa todos) ---
    List<MensagemLog> findByStatusAndDataEnvioBefore(String status, LocalDateTime dataEnvio);
}