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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.projeto.sistema.modelos.MensagemLog;
import com.projeto.sistema.repositorios.MensagemLogRepositorio;

@Controller
@RequestMapping("/mensagens")
public class MensagemControle {

    @Autowired
    private MensagemLogRepositorio mensagemRepositorio;

    @GetMapping("/enviadas")
    public ModelAndView listarEnviadas(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "comAnexo", required = false) Boolean comAnexo,
            @RequestParam(value = "busca", required = false) String busca,
            @RequestParam(value = "data", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        
        ModelAndView mv = new ModelAndView("mensagens/enviadas");
        
        // Configura Paginação (8 itens por página, ordenado por data decrescente)
        PageRequest pageRequest = PageRequest.of(page, 8, Sort.by("dataEnvio").descending());
        
        // Tratamento de datas para o filtro
        LocalDateTime inicio = (data != null) ? data.atStartOfDay() : null;
        LocalDateTime fim = (data != null) ? data.atTime(LocalTime.MAX) : null;

        // Busca no banco usando o novo método do repositório
        Page<MensagemLog> paginaMensagens = mensagemRepositorio.filtrarMensagens(comAnexo, busca, inicio, fim, pageRequest);
        
        mv.addObject("listaMensagens", paginaMensagens.getContent());
        mv.addObject("paginaAtual", page);
        mv.addObject("totalPaginas", paginaMensagens.getTotalPages());
        mv.addObject("totalItens", paginaMensagens.getTotalElements());
        
        // Devolve os filtros para a tela manter o estado
        mv.addObject("filtroAnexo", comAnexo);
        mv.addObject("filtroBusca", busca);
        mv.addObject("filtroData", data);
        mv.addObject("paginaAtiva", "mensagens");
        
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

    // Ação de Favoritar (chamada pelo JavaScript)
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
}