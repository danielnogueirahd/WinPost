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
    
    // --- QUERY PRINCIPAL ATUALIZADA (COM IMPORTANTE) ---
    // Agora aceita 'favorito', 'importante' e faz a busca inteligente
    @Query("SELECT m FROM MensagemLog m WHERE " +
           "(:pasta IS NULL OR m.pasta = :pasta) AND " + 
           "(:favorito IS NULL OR m.favorito = :favorito) AND " +
           "(:importante IS NULL OR m.importante = :importante) AND " + // <--- NOVO
           "(:temAnexo IS NULL OR (:temAnexo = true AND m.nomesAnexos IS NOT NULL AND m.nomesAnexos != '')) AND " +
           "(:termo IS NULL OR (lower(m.nomeGrupoDestino) LIKE lower(concat('%', :termo, '%')) OR lower(m.assunto) LIKE lower(concat('%', :termo, '%')))) AND " +
           "(:dataInicio IS NULL OR m.dataEnvio >= :dataInicio) AND " +
           "(:dataFim IS NULL OR m.dataEnvio <= :dataFim)")
    Page<MensagemLog> filtrarMensagens(
            @Param("pasta") String pasta, 
            @Param("favorito") Boolean favorito,
            @Param("importante") Boolean importante, // <--- NOVO PARÂMETRO
            @Param("temAnexo") Boolean temAnexo,
            @Param("termo") String termo,        
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable);
            
    // --- CONTADORES PARA O MENU ---
    
    // Conta total de mensagens em uma pasta (ex: Total de Enviadas, Lixeira)
    long countByPasta(String pasta);
    
    // Conta mensagens NÃO LIDAS na pasta (Badge vermelho da Entrada)
    long countByPastaAndLidaFalse(String pasta);
    
    // Conta quantos favoritos existem no total
    long countByFavoritoTrue();
    
    // Conta quantos marcados como IMPORTANTE existem
    long countByImportanteTrue(); // <--- NOVO MÉTODO
    
    // --- MÉTODOS AUXILIARES ---
    
    long countByLidaFalse(); 
    
    List<MensagemLog> findTop5ByLidaFalseOrderByDataEnvioDesc();
    
    List<MensagemLog> findByDataEnvioBetween(LocalDateTime dataInicio, LocalDateTime dataFim);

    List<MensagemLog> findByStatusAndDataEnvioBefore(String status, LocalDateTime dataEnvio);
}