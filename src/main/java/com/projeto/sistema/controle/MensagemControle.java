package com.projeto.sistema.controle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.projeto.sistema.modelos.MensagemLog;
import com.projeto.sistema.repositorios.MensagemLogRepositorio;

import java.util.Optional;

@Controller
@RequestMapping("/mensagens")
public class MensagemControle {

    @Autowired
    private MensagemLogRepositorio mensagemRepositorio;

    @GetMapping("/enviadas")
    public ModelAndView listarEnviadas() {
        ModelAndView mv = new ModelAndView("mensagens/enviadas");
        mv.addObject("listaMensagens", mensagemRepositorio.findAllByOrderByDataEnvioDesc());
        mv.addObject("paginaAtiva", "mensagens"); // Para marcar no menu lateral (precisa ajustar o sidebar depois)
        return mv;
    }
    
    @GetMapping("/detalhes/{id}")
    @ResponseBody
    public MensagemLog getDetalhes(@PathVariable Long id) {
        MensagemLog log = mensagemRepositorio.findById(id).orElse(new MensagemLog());
        
        // SE NÃO ESTIVER LIDA, MARCA COMO LIDA E SALVA
        if (!log.isLida()) {
            log.setLida(true);
            mensagemRepositorio.save(log);
        }
        
        return log;
    }
}