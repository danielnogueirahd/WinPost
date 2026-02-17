package com.projeto.sistema.controle;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
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

    // =========================================================================
    // MÉTODO CORRIGIDO: FILTRA PELA DATA DE CADASTRO (DATA COMPLETA)
    // =========================================================================
    private List<Contatos> filtrarPorPeriodoCadastro(List<Contatos> contatos, LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null && dataFim == null) {
            return contatos; // Se não tem filtro de data preenchido na tela, devolve a lista original
        }

        List<Contatos> filtrados = new ArrayList<>();

        for(Contatos c : contatos) {
            LocalDate cadastro = c.getDataCadastro();
            
            if (cadastro != null) {
                // Verifica se a data de cadastro está dentro do intervalo
                boolean validoInicio = (dataInicio == null) || !cadastro.isBefore(dataInicio);
                boolean validoFim = (dataFim == null) || !cadastro.isAfter(dataFim);

                if (validoInicio && validoFim) {
                    filtrados.add(c);
                }
            }
        }
        return filtrados;
    }

    // =========================================================================

    @GetMapping("/relatorio/pdf")
    public ResponseEntity<InputStreamResource> relatorioContatosPdf(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "cidade", required = false) String cidade,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "grupoId", required = false) Long grupoId,
            @RequestParam(value = "dataInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(value = "dataFim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        
        if (nome != null && nome.isEmpty()) nome = null;
        if (cidade != null && cidade.isEmpty()) cidade = null;
        if (estado != null && estado.isEmpty()) estado = null;

        // 1. Busca no banco usando os parâmetros de texto (nome, cidade, estado, grupoId)
        List<Contatos> contatos = contatosRepositorio.filtrarRelatorio(nome, cidade, estado, grupoId);
        
        // 2. Filtra pelo período de CADASTRO
        contatos = filtrarPorPeriodoCadastro(contatos, dataInicio, dataFim);
        
        ByteArrayInputStream bis = relatorioService.gerarRelatorioContatos(contatos);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=relatorio_contatos.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
    
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

        // 2. Busca os contatos usando o MESMO filtro do banco
        List<Contatos> contatos = contatosRepositorio.filtrarRelatorio(nome, cidade, estado, grupoId);
        
        // 3. Aplica o filtro de CADASTRO
        contatos = filtrarPorPeriodoCadastro(contatos, dataInicio, dataFim);
        
        // 4. Chama o novo serviço de etiquetas
        ByteArrayInputStream bis = relatorioService.gerarEtiquetas(contatos);

        // 5. Configura o download do PDF
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=etiquetas_contatos.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}