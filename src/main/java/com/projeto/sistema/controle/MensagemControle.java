package com.projeto.sistema.controle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
import com.projeto.sistema.repositorios.MensagemLogRepositorio;

@Controller
@RequestMapping("/mensagens")
public class MensagemControle {

    @Autowired
    private MensagemLogRepositorio mensagemRepositorio;

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
        
        // Paginação
        PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("dataEnvio").descending());
        
        // Filtros de Data
        LocalDateTime inicio = (data != null) ? data.atStartOfDay() : null;
        LocalDateTime fim = (data != null) ? data.atTime(LocalTime.MAX) : null;

        // --- LÓGICA DE ROTEAMENTO INTELIGENTE ---
        String filtroPastaBanco = null;
        Boolean filtroFavorito = null;
        Boolean filtroImportante = null;

        switch (pastaNormalizada) {
            case "FAVORITOS":
                // Pasta virtual: Ignora a pasta física e busca onde favorito = true
                filtroFavorito = true;
                break;
            case "IMPORTANTE":
                // Pasta virtual: Busca onde importante = true
                filtroImportante = true;
                break;
            case "LIXEIRA":
                // Pasta física: Busca itens marcados como LIXEIRA
                filtroPastaBanco = "LIXEIRA";
                break;
            case "TODAS":
                // Admin mode: vê tudo sem filtro
                break;
            default:
                // Comportamento padrão: busca pela coluna 'pasta' (ENVIADAS, ENTRADA)
                filtroPastaBanco = pastaNormalizada;
                break;
        }

        // Busca no banco usando a Query atualizada (agora com o parametro 'importante')
        Page<MensagemLog> paginaMensagens = mensagemRepositorio.filtrarMensagens(
            filtroPastaBanco, 
            filtroFavorito, 
            filtroImportante, // <--- Novo parâmetro
            comAnexo, 
            busca, 
            inicio, 
            fim, 
            pageRequest
        );
        
        // --- PREENCHENDO A TELA ---
        mv.addObject("listaMensagens", paginaMensagens.getContent());
        mv.addObject("paginaAtual", page);
        mv.addObject("totalPaginas", paginaMensagens.getTotalPages());
        mv.addObject("totalItens", paginaMensagens.getTotalElements());
        
        // Mantém filtros visuais
        mv.addObject("filtroAnexo", comAnexo);
        mv.addObject("termoBusca", busca);
        mv.addObject("filtroData", data);
        mv.addObject("pastaAtiva", pastaNormalizada);
        
        // --- CONTADORES PARA O MENU LATERAL ---
        mv.addObject("cntEntrada", mensagemRepositorio.countByPastaAndLidaFalse("ENTRADA"));
        mv.addObject("cntEnviadas", mensagemRepositorio.countByPasta("ENVIADAS"));
        mv.addObject("cntFavoritos", mensagemRepositorio.countByFavoritoTrue());
        mv.addObject("cntImportante", mensagemRepositorio.countByImportanteTrue()); // Novo
        mv.addObject("cntLixeira", mensagemRepositorio.countByPasta("LIXEIRA"));   // Novo
        
        return mv;
    }
    
    // --- DETALHES (Modal) ---
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

    // --- AÇÃO: FAVORITAR (Estrela) ---
    @PostMapping("/favoritar/{id}")
    @ResponseBody
    public ResponseEntity<Boolean> toggleFavorito(@PathVariable Long id) {
        return mensagemRepositorio.findById(id)
            .map(msg -> {
                msg.setFavorito(!msg.isFavorito());
                mensagemRepositorio.save(msg);
                return ResponseEntity.ok(msg.isFavorito());
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // --- AÇÃO: IMPORTANTE (Flag) ---
    @PostMapping("/importante/{id}")
    @ResponseBody
    public ResponseEntity<Boolean> toggleImportante(@PathVariable Long id) {
        return mensagemRepositorio.findById(id)
            .map(msg -> {
                msg.setImportante(!msg.isImportante());
                mensagemRepositorio.save(msg);
                return ResponseEntity.ok(msg.isImportante());
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // --- AÇÃO: LIXEIRA (Mover para o lixo) ---
    @PostMapping("/lixeira/{id}")
    @ResponseBody
    public ResponseEntity<Void> moverParaLixeira(@PathVariable Long id) {
        return mensagemRepositorio.findById(id).map(msg -> {
            msg.setPasta("LIXEIRA"); // Apenas muda o status da pasta, não deleta do banco
            mensagemRepositorio.save(msg);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
 // ... (Mantenha o código anterior)

    // --- AÇÃO: RESTAURAR (Tira da Lixeira) ---
    @PostMapping("/restaurar/{id}")
    @ResponseBody
    public ResponseEntity<Void> restaurarMensagem(@PathVariable Long id) {
        return mensagemRepositorio.findById(id).map(msg -> {
            msg.setPasta("ENVIADAS"); // Volta para o status normal
            mensagemRepositorio.save(msg);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- AÇÃO: EXCLUIR DEFINITIVAMENTE (Perigo) ---
    @DeleteMapping("/excluir/{id}") // Note que usamos DeleteMapping, mas o jQuery vai chamar via AJAX
    @ResponseBody
    public ResponseEntity<Void> excluirPermanente(@PathVariable Long id) {
        if(mensagemRepositorio.existsById(id)) {
            mensagemRepositorio.deleteById(id); // Apaga do banco para sempre
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    
}