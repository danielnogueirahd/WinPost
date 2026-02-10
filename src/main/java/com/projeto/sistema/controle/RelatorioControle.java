package com.projeto.sistema.controle;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.projeto.sistema.modelos.Contatos;
import com.projeto.sistema.repositorios.ContatosRepositorio;
import com.projeto.sistema.servicos.RelatorioService;

@Controller
public class RelatorioControle {

    @Autowired
    private ContatosRepositorio contatosRepositorio;
    
    @Autowired
    private RelatorioService relatorioService;

    @GetMapping("/relatorio/pdf")
    public ResponseEntity<InputStreamResource> relatorioContatosPdf(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "cidade", required = false) String cidade,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "grupoId", required = false) Long grupoId, // <--- Adicionado
            @RequestParam(value = "dataInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(value = "dataFim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        
        if (nome != null && nome.isEmpty()) nome = null;
        if (cidade != null && cidade.isEmpty()) cidade = null;
        if (estado != null && estado.isEmpty()) estado = null;

        // Passando grupoId para o repositório
        List<Contatos> contatos = contatosRepositorio.filtrarRelatorio(nome, cidade, estado, grupoId, dataInicio, dataFim);
        
        ByteArrayInputStream bis = relatorioService.gerarRelatorioContatos(contatos);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=relatorio_contatos.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
    
 // ... (Método do relatório normal acima)

    // NOVO MÉTODO: Geração de Etiquetas
    @GetMapping("/relatorio/etiquetas")
    public ResponseEntity<InputStreamResource> etiquetasContatosPdf(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "cidade", required = false) String cidade,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "grupoId", required = false) Long grupoId,
            @RequestParam(value = "dataInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(value = "dataFim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        
        // 1. Limpeza dos filtros (igual ao relatório)
        if (nome != null && nome.isEmpty()) nome = null;
        if (cidade != null && cidade.isEmpty()) cidade = null;
        if (estado != null && estado.isEmpty()) estado = null;

        // 2. Busca os contatos usando o MESMO filtro do relatório
        // Isso é ótimo, pois garante que se você filtrou na tela, a etiqueta sai igual.
        List<Contatos> contatos = contatosRepositorio.filtrarRelatorio(nome, cidade, estado, grupoId, dataInicio, dataFim);
        
        // 3. Chama o novo serviço de etiquetas
        ByteArrayInputStream bis = relatorioService.gerarEtiquetas(contatos);

        // 4. Configura o download do PDF
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=etiquetas_contatos.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
    }