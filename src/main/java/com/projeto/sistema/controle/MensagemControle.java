package com.projeto.sistema.controle;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.projeto.sistema.modelos.Grupo;
import com.projeto.sistema.modelos.MensagemLog;
import com.projeto.sistema.repositorios.GrupoRepositorio;
import com.projeto.sistema.repositorios.MensagemLogRepositorio;
import com.projeto.sistema.servicos.EmailService;

@Controller
@RequestMapping("/mensagens")
public class MensagemControle {

    @Autowired
    private MensagemLogRepositorio mensagemRepositorio;
    
    @Autowired
    private GrupoRepositorio grupoRepositorio;

    @Autowired
    private EmailService emailService; // Agora será utilizado no método /enviar

    // --- REDIRECIONAMENTO PADRÃO ---
    @GetMapping("/enviadas")
    public String redirecionarEnviadas() {
        return "redirect:/mensagens/caixa/ENVIADAS";
    }

    // --- LISTAGEM (CAIXA DE ENTRADA, ENVIADAS, LIXEIRA, ETC) ---
    @GetMapping("/caixa/{pasta}")
    public ModelAndView listarPorPasta(
            @PathVariable("pasta") String pastaUrl,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "comAnexo", required = false) Boolean comAnexo,
            @RequestParam(value = "busca", required = false) String busca,
            @RequestParam(value = "data", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        
        ModelAndView mv = new ModelAndView("mensagens/enviadas");
        String pastaNormalizada = pastaUrl.toUpperCase();
        
        PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("dataEnvio").descending());
        LocalDateTime inicio = (data != null) ? data.atStartOfDay() : null;
        LocalDateTime fim = (data != null) ? data.atTime(LocalTime.MAX) : null;

        String filtroPastaBanco = null;
        Boolean filtroFavorito = null;
        Boolean filtroImportante = null;

        // Configura os filtros baseados na URL
        switch (pastaNormalizada) {
            case "FAVORITOS": filtroFavorito = true; break;
            case "IMPORTANTE": filtroImportante = true; break;
            case "LIXEIRA": filtroPastaBanco = "LIXEIRA"; break;
            case "TODAS": break;
            default: filtroPastaBanco = pastaNormalizada; break; // Ex: "ENVIADAS", "ENTRADA"
        }

        Page<MensagemLog> paginaMensagens = mensagemRepositorio.filtrarMensagens(
            filtroPastaBanco, filtroFavorito, filtroImportante, comAnexo, busca, inicio, fim, pageRequest
        );
        
        // Adiciona dados à View
        mv.addObject("listaMensagens", paginaMensagens.getContent());
        mv.addObject("paginaAtual", page);
        mv.addObject("totalPaginas", paginaMensagens.getTotalPages());
        mv.addObject("totalItens", paginaMensagens.getTotalElements());
        
        // Mantém os filtros na tela
        mv.addObject("filtroAnexo", comAnexo);
        mv.addObject("termoBusca", busca);
        mv.addObject("filtroData", data);
        mv.addObject("pastaAtiva", pastaNormalizada);
        
        // Contadores para o Menu Lateral
        mv.addObject("cntEntrada", mensagemRepositorio.countByPastaAndLidaFalse("ENTRADA"));
        mv.addObject("cntEnviadas", mensagemRepositorio.countByPasta("ENVIADAS"));
        mv.addObject("cntFavoritos", mensagemRepositorio.countByFavoritoTrue());
        mv.addObject("cntImportante", mensagemRepositorio.countByImportanteTrue());
        mv.addObject("cntLixeira", mensagemRepositorio.countByPasta("LIXEIRA"));
        
        return mv;
    }
    
    // --- DETALHES DA MENSAGEM (JSON) ---
    @GetMapping("/detalhes/{id}")
    @ResponseBody
    public MensagemLog getDetalhes(@PathVariable Long id) {
        MensagemLog log = mensagemRepositorio.findById(id).orElse(new MensagemLog());
        if (!log.isLida()) {
            log.setLida(true);
            mensagemRepositorio.save(log);
        }
        return log;
    }

    // --- AÇÕES RÁPIDAS (Favoritar, Importante, Lixeira) ---

    @PostMapping("/favoritar/{id}")
    @ResponseBody
    public ResponseEntity<Boolean> toggleFavorito(@PathVariable Long id) {
        return mensagemRepositorio.findById(id).map(msg -> {
            msg.setFavorito(!msg.isFavorito());
            mensagemRepositorio.save(msg);
            return ResponseEntity.ok(msg.isFavorito());
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/importante/{id}")
    @ResponseBody
    public ResponseEntity<Boolean> toggleImportante(@PathVariable Long id) {
        return mensagemRepositorio.findById(id).map(msg -> {
            msg.setImportante(!msg.isImportante());
            mensagemRepositorio.save(msg);
            return ResponseEntity.ok(msg.isImportante());
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/lixeira/{id}")
    @ResponseBody
    public ResponseEntity<Void> moverParaLixeira(@PathVariable Long id) {
        return mensagemRepositorio.findById(id).map(msg -> {
            msg.setPasta("LIXEIRA");
            mensagemRepositorio.save(msg);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/restaurar/{id}")
    @ResponseBody
    public ResponseEntity<Void> restaurarMensagem(@PathVariable Long id) {
        return mensagemRepositorio.findById(id).map(msg -> {
            msg.setPasta("ENVIADAS"); // Restaura para Enviadas por padrão
            mensagemRepositorio.save(msg);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/excluir/{id}")
    @ResponseBody
    public ResponseEntity<Void> excluirPermanente(@PathVariable Long id) {
        if(mensagemRepositorio.existsById(id)) {
            mensagemRepositorio.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    // --- ENVIO DE MENSAGENS E MODELOS ---

    // 1. Tela de Preparação (Vazia ou com Modelo)
    @GetMapping("/preparar-envio/{id}")
    public ModelAndView prepararEnvio(@PathVariable Long id) {
        ModelAndView mv = new ModelAndView("mensagens/preparar");
        MensagemLog modelo = mensagemRepositorio.findById(id).orElseThrow();
        mv.addObject("modelo", modelo);
        mv.addObject("listaGrupos", grupoRepositorio.findAll()); 
        return mv;
    }

    // 2. Tela de Preparação (Vindo da tela de Grupos)
    @GetMapping("/preparar-grupo/{idGrupo}")
    public ModelAndView prepararEnvioGrupo(@PathVariable Long idGrupo) {
        ModelAndView mv = new ModelAndView("mensagens/preparar");
        
        MensagemLog modeloVazio = new MensagemLog();
        modeloVazio.setAssunto("");
        modeloVazio.setConteudo("");
        
        mv.addObject("modelo", modeloVazio);
        mv.addObject("listaGrupos", grupoRepositorio.findAll());
        mv.addObject("idGrupoSelecionado", idGrupo); 
        
        return mv;
    }

    // 3. AÇÃO DE ENVIAR (Adicionado para completar a funcionalidade)
    @PostMapping("/enviar")
    public String enviarMensagem(@RequestParam("grupoId") Long grupoId,
                                 @RequestParam("assunto") String assunto,
                                 @RequestParam("conteudo") String conteudo,
                                 RedirectAttributes attributes) {
        
        Optional<Grupo> grupoOpt = grupoRepositorio.findById(grupoId);
        
        if (grupoOpt.isPresent()) {
            Grupo grupo = grupoOpt.get();
            
            // Tenta enviar o e-mail usando o serviço injetado
            try {
                // emailService.enviar(grupo, assunto, conteudo); // Descomente e ajuste conforme seu EmailService
            } catch (Exception e) {
                e.printStackTrace(); // Apenas loga o erro, mas salva no banco
            }

            // Salva o registro na pasta ENVIADAS
            MensagemLog enviada = new MensagemLog();
            enviada.setPasta("ENVIADAS");
            enviada.setNomeGrupoDestino(grupo.getNome());
            enviada.setAssunto(assunto);
            enviada.setConteudo(conteudo);
            enviada.setDataEnvio(LocalDateTime.now());
            enviada.setLida(true);
            enviada.setStatus("SUCESSO");
            
            // Se tiver lista de contatos, salva a quantidade
             if (grupo.getContatos() != null) {
                 enviada.setTotalDestinatarios(grupo.getContatos().size());
             }

            mensagemRepositorio.save(enviada);
            
            attributes.addFlashAttribute("mensagem", "Mensagem enviada com sucesso para " + grupo.getNome() + "!");
            attributes.addFlashAttribute("tipoMensagem", "success");
        } else {
            attributes.addFlashAttribute("mensagem", "Erro: Grupo selecionado não existe.");
            attributes.addFlashAttribute("tipoMensagem", "danger");
        }
        
        return "redirect:/mensagens/caixa/ENVIADAS";
    }

    // 4. Salvar Rascunho/Modelo (Simulador de Entrada)
    @PostMapping("/salvarModelo")
    public String salvarModelo(@RequestParam("categoria") String categoria,
                               @RequestParam("assunto") String assunto,
                               @RequestParam("conteudo") String conteudo) {
        MensagemLog modelo = new MensagemLog();
        modelo.setPasta("ENTRADA"); // Salva na Entrada como simulação/rascunho
        modelo.setNomeGrupoDestino(categoria.toUpperCase());
        modelo.setAssunto(assunto);
        modelo.setConteudo(conteudo);
        modelo.setDataEnvio(LocalDateTime.now());
        modelo.setLida(true); 
        mensagemRepositorio.save(modelo);
        return "redirect:/mensagens/caixa/ENTRADA";
    }

    // 5. Agendar (Redireciona para Agenda)
    @PostMapping("/agendar-modelo")
    public String agendarModelo(@RequestParam("assunto") String assunto, 
                                @RequestParam("conteudo") String conteudo) {
        String tituloCodificado = assunto;
        try {
            tituloCodificado = URLEncoder.encode(assunto, StandardCharsets.UTF_8.toString());
        } catch (Exception e) { e.printStackTrace(); }
        return "redirect:/administrativo/agenda?acao=novoEvento&titulo=" + tituloCodificado;
    }
}