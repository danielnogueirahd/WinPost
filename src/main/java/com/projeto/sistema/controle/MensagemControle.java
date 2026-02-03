package com.projeto.sistema.controle;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

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
    private EmailService emailService;

    // Redirecionamento padrão
    @GetMapping("/enviadas")
    public String redirecionarEnviadas() {
        return "redirect:/mensagens/caixa/ENVIADAS";
    }

    // --- ROTA GENÉRICA: Lida com Entrada, Enviadas, Favoritos, Importante, Lixeira ---
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

        switch (pastaNormalizada) {
            case "FAVORITOS": filtroFavorito = true; break;
            case "IMPORTANTE": filtroImportante = true; break;
            case "LIXEIRA": filtroPastaBanco = "LIXEIRA"; break;
            case "TODAS": break;
            default: filtroPastaBanco = pastaNormalizada; break;
        }

        Page<MensagemLog> paginaMensagens = mensagemRepositorio.filtrarMensagens(
            filtroPastaBanco, filtroFavorito, filtroImportante, comAnexo, busca, inicio, fim, pageRequest
        );
        
        mv.addObject("listaMensagens", paginaMensagens.getContent());
        mv.addObject("paginaAtual", page);
        mv.addObject("totalPaginas", paginaMensagens.getTotalPages());
        mv.addObject("totalItens", paginaMensagens.getTotalElements());
        
        mv.addObject("filtroAnexo", comAnexo);
        mv.addObject("termoBusca", busca);
        mv.addObject("filtroData", data);
        mv.addObject("pastaAtiva", pastaNormalizada);
        
        mv.addObject("cntEntrada", mensagemRepositorio.countByPastaAndLidaFalse("ENTRADA"));
        mv.addObject("cntEnviadas", mensagemRepositorio.countByPasta("ENVIADAS"));
        mv.addObject("cntFavoritos", mensagemRepositorio.countByFavoritoTrue());
        mv.addObject("cntImportante", mensagemRepositorio.countByImportanteTrue());
        mv.addObject("cntLixeira", mensagemRepositorio.countByPasta("LIXEIRA"));
        
        return mv;
    }
    
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
            msg.setPasta("ENVIADAS");
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
    
    // --- MÉTODOS NOVOS (QUE ESTAVAM FALTANDO) ---

    @PostMapping("/salvarModelo")
    public String salvarModelo(@RequestParam("categoria") String categoria,
                               @RequestParam("assunto") String assunto,
                               @RequestParam("conteudo") String conteudo) {
        MensagemLog modelo = new MensagemLog();
        modelo.setPasta("ENTRADA");
        modelo.setNomeGrupoDestino(categoria.toUpperCase());
        modelo.setAssunto(assunto);
        modelo.setConteudo(conteudo);
        modelo.setDataEnvio(LocalDateTime.now());
        modelo.setLida(true); 
        mensagemRepositorio.save(modelo);
        return "redirect:/mensagens/caixa/ENTRADA";
    }

    @GetMapping("/preparar-envio/{id}")
    public ModelAndView prepararEnvio(@PathVariable Long id) {
        ModelAndView mv = new ModelAndView("mensagens/preparar");
        MensagemLog modelo = mensagemRepositorio.findById(id).orElseThrow();
        mv.addObject("modelo", modelo);
        mv.addObject("listaGrupos", grupoRepositorio.findAll()); 
        return mv;
    }

    

    @PostMapping("/agendar-modelo")
    public String agendarModelo(@RequestParam("assunto") String assunto, 
                                @RequestParam("conteudo") String conteudo) {
        String tituloCodificado = assunto;
        try {
            tituloCodificado = URLEncoder.encode(assunto, StandardCharsets.UTF_8.toString());
        } catch (Exception e) { e.printStackTrace(); }
        return "redirect:/administrativo/agenda?acao=novoEvento&titulo=" + tituloCodificado;
    }
    
 // ... outros imports e métodos ...

    // NOVO MÉTODO: Inicia o envio a partir da tela de Grupos
    @GetMapping("/preparar-grupo/{idGrupo}")
    public ModelAndView prepararEnvioGrupo(@PathVariable Long idGrupo) {
        ModelAndView mv = new ModelAndView("mensagens/preparar");
        
        // Cria um modelo vazio para o formulário não quebrar
        MensagemLog modeloVazio = new MensagemLog();
        modeloVazio.setAssunto("");
        modeloVazio.setConteudo("");
        
        mv.addObject("modelo", modeloVazio);
        mv.addObject("listaGrupos", grupoRepositorio.findAll());
        
        // Passamos o ID do grupo para o HTML selecionar automaticamente
        mv.addObject("idGrupoSelecionado", idGrupo); 
        
        return mv;
    }
}