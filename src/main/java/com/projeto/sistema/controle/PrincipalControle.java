package com.projeto.sistema.controle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // <-- IMPORT DO CRACHÁ
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.projeto.sistema.modelos.Lembrete;
import com.projeto.sistema.modelos.UF;
import com.projeto.sistema.modelos.UsuarioLogado; // <-- IMPORT DO CRACHÁ
import com.projeto.sistema.repositorios.ContatosRepositorio;
import com.projeto.sistema.repositorios.GrupoRepositorio;
import com.projeto.sistema.repositorios.LembreteRepositorio;
import com.projeto.sistema.repositorios.MensagemLogRepositorio;
import com.projeto.sistema.repositorios.UsuarioRepositorio;

@Controller
public class PrincipalControle {

    @Autowired 
    private ContatosRepositorio contatosRepositorio;
    
    @Autowired 
    private GrupoRepositorio grupoRepositorio;
    
    @Autowired 
    private UsuarioRepositorio usuarioRepositorio;
    
    @Autowired 
    private MensagemLogRepositorio mensagemRepositorio; 
    
    @Autowired 
    private LembreteRepositorio lembreteRepositorio;   
    
    @Autowired 
    private PasswordEncoder passwordEncoder;

    // --- DASHBOARD (Home) ---
    @GetMapping("/administrativo")
    public ModelAndView acessarPrincipal(@AuthenticationPrincipal UsuarioLogado usuarioLogado) { // <-- RECEBE O CRACHÁ
        ModelAndView mv = new ModelAndView("administrativo/home");
        
        // 1. Total de Contatos DA EMPRESA
        mv.addObject("totalContatos", contatosRepositorio.findByEmpresa(usuarioLogado.getEmpresa()).size()); 
        
        // 2. Últimos cadastrados DA EMPRESA (Busca os 10 mais recentes)
        mv.addObject("ultimosContatos", contatosRepositorio.findTop10ByEmpresaOrderByIdDesc(usuarioLogado.getEmpresa()));
        
        // 3. Total de Grupos DA EMPRESA
        mv.addObject("totalGrupos", grupoRepositorio.findByEmpresa(usuarioLogado.getEmpresa()).size());
        
        // 4. Mensagens Enviadas DA EMPRESA (O ERRO ESTAVA AQUI)
        mv.addObject("mensagensEnviadas", mensagemRepositorio.countByPastaAndEmpresa("ENVIADAS", usuarioLogado.getEmpresa()));
        
        // 5. Lembretes e Agenda de Hoje (Em breve também será filtrado por empresa)
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(LocalTime.MAX);
        
        // Busca lembretes entre 00:00 e 23:59 de hoje
        List<Lembrete> agendaHoje = lembreteRepositorio.findByDataHoraBetweenAndEmpresa(inicioDia, fimDia, usuarioLogado.getEmpresa());
        
        mv.addObject("lembretesHoje", agendaHoje.size()); // Quantidade para o Card colorido
        mv.addObject("agendaHoje", agendaHoje);            // Lista detalhada para o Widget lateral
        
        // Dados para Modais (Cadastro rápido e Relatórios)
        mv.addObject("listaEstados", UF.values());
        mv.addObject("listaGrupos", grupoRepositorio.findByEmpresa(usuarioLogado.getEmpresa())); 
        
        return mv;
    }
}