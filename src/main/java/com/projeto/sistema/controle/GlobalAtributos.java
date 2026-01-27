package com.projeto.sistema.controle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.projeto.sistema.modelos.Contatos;
import com.projeto.sistema.modelos.Lembrete;
import com.projeto.sistema.modelos.LembreteDTO;
import com.projeto.sistema.modelos.MensagemLog;
import com.projeto.sistema.repositorios.ContatosRepositorio;
import com.projeto.sistema.repositorios.GrupoRepositorio; // <--- 1. Import Adicionado
import com.projeto.sistema.repositorios.LembreteRepositorio;
import com.projeto.sistema.repositorios.MensagemLogRepositorio;

@ControllerAdvice
public class GlobalAtributos {

    @Autowired
    private MensagemLogRepositorio mensagemRepositorio;

    @Autowired
    private ContatosRepositorio contatosRepositorio;

    @Autowired
    private LembreteRepositorio lembreteRepositorio;
    
    @Autowired
    private GrupoRepositorio grupoRepositorio; // <--- 2. Injeção Adicionada

    // --- Parte Nova: Carrega os Grupos Globalmente para o Modal ---
    @ModelAttribute("listaGruposGlobal")
    public List<?> carregarGruposGlobais() {
        return grupoRepositorio.findAll();
    }
    // -------------------------------------------------------------

    @ModelAttribute("notificacoesNaoLidas")
    public long carregarContador() {
        return mensagemRepositorio.countByLidaFalse();
    }

    @ModelAttribute("listaNotificacoes")
    public List<MensagemLog> carregarLista() {
        return mensagemRepositorio.findTop5ByLidaFalseOrderByDataEnvioDesc();
    }
    
    @ModelAttribute("listaLembretes")
    public List<LembreteDTO> carregarLembretesFuturos() {
        List<LembreteDTO> lembretes = new ArrayList<>();
        
        // Data alvo: AMANHÃ
        LocalDate amanha = LocalDate.now().plusDays(1);
        
        // --- 1. Busca Aniversariantes de Amanhã ---
        List<Contatos> aniversariantesAmanha = contatosRepositorio.findByDiaEMesAniversario(
            amanha.getDayOfMonth(), 
            amanha.getMonthValue()
        );

        for (Contatos c : aniversariantesAmanha) {
            lembretes.add(new LembreteDTO(
                "Aniversário Amanhã",
                "Não esqueça de parabenizar " + c.getNome(),
                "NIVER",
                amanha,
                c.getId()
            ));
        }

        // --- 2. Busca Tarefas/Lembretes da Agenda para Amanhã ---
        LocalDateTime inicioDia = amanha.atStartOfDay();
        LocalDateTime fimDia = amanha.atTime(LocalTime.MAX);
        
        List<Lembrete> tarefasAmanha = lembreteRepositorio.findByDataHoraBetween(inicioDia, fimDia);

        for (Lembrete l : tarefasAmanha) {
            Long idReferencia = (l.getContato() != null) ? l.getContato().getId() : 0L;

            lembretes.add(new LembreteDTO(
                l.getTitulo(),
                l.getDescricao() != null ? l.getDescricao() : "Sem descrição",
                l.getTipo(),
                amanha,
                idReferencia
            ));
        }

        return lembretes;
    }
    
    @ModelAttribute("qtdLembretes")
    public int contarLembretes() {
        return carregarLembretesFuturos().size();
    }
    
}