package com.projeto.sistema.repositorios;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.projeto.sistema.modelos.MensagemLog;

public interface MensagemLogRepositorio extends JpaRepository<MensagemLog, Long> {
	
    // Mantido para compatibilidade
    List<MensagemLog> findAllByOrderByDataEnvioDesc();
    
    // NOVO MÉTODO: Suporta Filtros (Anexo, Grupo, Data) e Paginação
    @Query("SELECT m FROM MensagemLog m WHERE " +
           "(:temAnexo IS NULL OR (:temAnexo = true AND m.nomesAnexos IS NOT NULL AND m.nomesAnexos != '')) AND " +
           "(:grupo IS NULL OR lower(m.nomeGrupoDestino) LIKE lower(concat('%', :grupo, '%'))) AND " +
           "(:dataInicio IS NULL OR m.dataEnvio >= :dataInicio) AND " +
           "(:dataFim IS NULL OR m.dataEnvio <= :dataFim)")
    Page<MensagemLog> filtrarMensagens(
            @Param("temAnexo") Boolean temAnexo,
            @Param("grupo") String grupo,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable);
}