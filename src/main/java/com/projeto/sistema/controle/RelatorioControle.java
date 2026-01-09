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
            @RequestParam(value = "dataInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(value = "dataFim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        
        // Trata strings vazias como null para o banco ignorar o filtro
        if (nome != null && nome.isEmpty()) nome = null;
        if (cidade != null && cidade.isEmpty()) cidade = null;
        if (estado != null && estado.isEmpty()) estado = null;

        // Busca filtrada
        List<Contatos> contatos = contatosRepositorio.filtrarRelatorio(nome, cidade, estado, dataInicio, dataFim);
        
        ByteArrayInputStream bis = relatorioService.gerarRelatorioContatos(contatos);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=relatorio_personalizado.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}