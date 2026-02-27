package com.projeto.sistema.controle;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.projeto.sistema.modelos.Lembrete;
import com.projeto.sistema.modelos.UF;
import com.projeto.sistema.modelos.Usuario;
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
    public ModelAndView acessarPrincipal() {
        ModelAndView mv = new ModelAndView("administrativo/home");
        
        // 1. Total de Contatos
        mv.addObject("totalContatos", contatosRepositorio.count()); 
        
        // 2. Últimos cadastrados (Busca os 10 mais recentes pelo ID)
        mv.addObject("ultimosContatos", contatosRepositorio.findTop10ByOrderByIdDesc());
        
        // 3. Total de Grupos
        mv.addObject("totalGrupos", grupoRepositorio.count());
        
        // 4. Mensagens Enviadas (Filtro corrigido para "ENVIADAS" conforme o banco)
        mv.addObject("mensagensEnviadas", mensagemRepositorio.countByPasta("ENVIADAS"));
        
        // 5. Lembretes e Agenda de Hoje
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(LocalTime.MAX);
        
        // Busca lembretes entre 00:00 e 23:59 de hoje
        List<Lembrete> agendaHoje = lembreteRepositorio.findByDataHoraBetween(inicioDia, fimDia);
        
        mv.addObject("lembretesHoje", agendaHoje.size()); // Quantidade para o Card colorido
        mv.addObject("agendaHoje", agendaHoje);           // Lista detalhada para o Widget lateral
        
        // Dados para Modais (Cadastro rápido e Relatórios)
        mv.addObject("listaEstados", UF.values());
        mv.addObject("listaGrupos", grupoRepositorio.findAll()); 
        
        return mv;
    }
    
    // --- PERFIL (Exibir) ---
    @GetMapping("/perfil")
    public ModelAndView perfil(Principal principal) {
        ModelAndView mv = new ModelAndView("administrativo/perfil");
        Usuario usuario = usuarioRepositorio.findByUsername(principal.getName());
        mv.addObject("usuario", usuario);
        return mv;
    }

    // --- PERFIL (Salvar Dados) ---
    @PostMapping("/perfil/salvar")
    public String salvarPerfil(Usuario usuarioForm, Principal principal, RedirectAttributes attributes) {
        Usuario usuarioBanco = usuarioRepositorio.findByUsername(principal.getName());
        
        // Atualiza apenas nome e email, mantendo a senha antiga
        usuarioBanco.setNome(usuarioForm.getNome());
        usuarioBanco.setEmail(usuarioForm.getEmail());
        
        usuarioRepositorio.save(usuarioBanco);
        
        attributes.addFlashAttribute("mensagem", "Dados atualizados com sucesso!");
        attributes.addFlashAttribute("tipoMensagem", "success");
        return "redirect:/perfil";
    }

    // --- PERFIL (Alterar Senha) ---
    @PostMapping("/perfil/senha")
    public String alterarSenha(@RequestParam("senhaAtual") String senhaAtual,
                               @RequestParam("novaSenha") String novaSenha,
                               Principal principal, 
                               RedirectAttributes attributes) {
        
        Usuario usuario = usuarioRepositorio.findByUsername(principal.getName());
        
        // Verifica se a senha atual confere
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            attributes.addFlashAttribute("mensagem", "Senha atual incorreta!");
            attributes.addFlashAttribute("tipoMensagem", "danger");
            return "redirect:/perfil";
        }
        
        // Criptografa e salva a nova senha
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepositorio.save(usuario);
        
        attributes.addFlashAttribute("mensagem", "Senha alterada com sucesso!");
        attributes.addFlashAttribute("tipoMensagem", "success");
        return "redirect:/perfil";
    }
}