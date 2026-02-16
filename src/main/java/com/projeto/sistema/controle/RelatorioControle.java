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
    // MÉTODO AUXILIAR PARA FILTRAR AS DATAS "DD/MM" DIRETAMENTE NO JAVA
    // =========================================================================
    private List<Contatos> filtrarPorData(List<Contatos> contatos, LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            return contatos; // Se não tem filtro de data preenchido na tela, devolve a lista original
        }

        List<Contatos> filtrados = new ArrayList<>();
        LocalDate inicioBase = LocalDate.of(2024, dataInicio.getMonthValue(), dataInicio.getDayOfMonth());
        LocalDate fimBase = LocalDate.of(2024, dataFim.getMonthValue(), dataFim.getDayOfMonth());
        boolean viraAno = inicioBase.isAfter(fimBase);

        for(Contatos c : contatos) {
            if(c.getDataNascimento() != null && c.getDataNascimento().length() >= 5) {
                try {
                    int dia = Integer.parseInt(c.getDataNascimento().substring(0, 2));
                    int mes = Integer.parseInt(c.getDataNascimento().substring(3, 5));
                    LocalDate niver = LocalDate.of(2024, mes, dia);
                    
                    if(viraAno) {
                        if(!niver.isBefore(inicioBase) || !niver.isAfter(fimBase)) filtrados.add(c);
                    } else {
                        if(!niver.isBefore(inicioBase) && !niver.isAfter(fimBase)) filtrados.add(c);
                    }
                } catch(Exception e) {
                    // Ignora contatos que tenham o formato de data em branco ou inválido
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
        
        // 2. Filtra pelo período das datas de aniversário via código Java
        contatos = filtrarPorData(contatos, dataInicio, dataFim);
        
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
        
        // 3. Aplica o MESMO filtro de data "DD/MM" do Java
        contatos = filtrarPorData(contatos, dataInicio, dataFim);
        
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