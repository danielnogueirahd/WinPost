package com.projeto.sistema.controle;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.projeto.sistema.repositorios.MensagemLogRepositorio;
import com.projeto.sistema.modelos.MensagemLog;

@ControllerAdvice
public class GlobalAtributos {

    @Autowired
    private MensagemLogRepositorio mensagemRepositorio;

    @ModelAttribute("notificacoesNaoLidas")
    public long carregarContador() {
        return mensagemRepositorio.countByLidaFalse();
    }

    // --- NOVO: Carrega a lista das últimas 5 mensagens ---
    @ModelAttribute("listaNotificacoes")
    public List<MensagemLog> carregarLista() {
        return mensagemRepositorio.findTop5ByLidaFalseOrderByDataEnvioDesc();
    }
}