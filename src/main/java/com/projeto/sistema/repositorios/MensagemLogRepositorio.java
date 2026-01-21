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
    
    // ATUALIZADO: Adicionado filtro por PASTA
    @Query("SELECT m FROM MensagemLog m WHERE " +
           "(:pasta IS NULL OR m.pasta = :pasta) AND " + 
           "(:temAnexo IS NULL OR (:temAnexo = true AND m.nomesAnexos IS NOT NULL AND m.nomesAnexos != '')) AND " +
           "(:grupo IS NULL OR lower(m.nomeGrupoDestino) LIKE lower(concat('%', :grupo, '%'))) AND " +
           "(:dataInicio IS NULL OR m.dataEnvio >= :dataInicio) AND " +
           "(:dataFim IS NULL OR m.dataEnvio <= :dataFim)")
    Page<MensagemLog> filtrarMensagens(
            @Param("pasta") String pasta, 
            @Param("temAnexo") Boolean temAnexo,
            @Param("grupo") String grupo,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable);
            
    // Contadores para o menu lateral
    long countByPasta(String pasta);
    
    long countByLidaFalse();
    
    // Busca as 5 mensagens mais recentes que NÃO foram lidas
    List<MensagemLog> findTop5ByLidaFalseOrderByDataEnvioDesc();
    
    // Busca mensagens enviadas dentro de um intervalo de datas
    List<MensagemLog> findByDataEnvioBetween(LocalDateTime dataInicio, LocalDateTime dataFim);

    // --- [NOVO] O MÉTODO QUE ESTAVA FALTANDO PARA O AGENDADOR FUNCIONAR ---
    // Busca mensagens com status específico (ex: "AGENDADO") e data anterior a agora
    List<MensagemLog> findByStatusAndDataEnvioBefore(String status, LocalDateTime dataEnvio);
}