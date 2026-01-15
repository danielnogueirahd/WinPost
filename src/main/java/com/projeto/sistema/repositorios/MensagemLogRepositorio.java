package com.projeto.sistema.repositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.projeto.sistema.modelos.MensagemLog;

public interface MensagemLogRepositorio extends JpaRepository<MensagemLog, Long> {
    
    // Busca todos ordenados por data (mais novos primeiro)
    List<MensagemLog> findAllByOrderByDataEnvioDesc();
}