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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.projeto.sistema.modelos.Contatos;
import com.projeto.sistema.modelos.UsuarioLogado;
import com.projeto.sistema.repositorios.ContatosRepositorio;
import com.projeto.sistema.servicos.RelatorioService;
import com.projeto.sistema.servicos.EtiquetaService; // <-- NOVO IMPORT


@Controller
@PreAuthorize("hasAuthority('RELATORIO_VISUALIZAR')")
public class RelatorioControle {

    @Autowired
    private ContatosRepositorio contatosRepositorio;
    
    @Autowired
    private RelatorioService relatorioService;

    @Autowired
    private EtiquetaService etiquetaService; // <-- INJETAMOS O NOSSO NOVO SERVIÇO AQUI

    // =========================================================================
    // MÉTODO: FILTRA PELA DATA DE CADASTRO
    // =========================================================================
    private List<Contatos> filtrarPorPeriodoCadastro(List<Contatos> contatos, LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null && dataFim == null) {
            return contatos; 
        }

        List<Contatos> filtrados = new ArrayList<>();

        for(Contatos c : contatos) {
            LocalDate cadastro = c.getDataCadastro();
            if (cadastro != null) {
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
    // RELATÓRIO NORMAL (PDF EM LISTA)
    // =========================================================================
    @GetMapping("/relatorio/pdf")
    public ResponseEntity<InputStreamResource> relatorioContatosPdf(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "cidade", required = false) String cidade,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "grupoId", required = false) Long grupoId,
            @RequestParam(value = "dataInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(value = "dataFim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @AuthenticationPrincipal UsuarioLogado usuarioLogado) { 
        
        if (nome != null && nome.isEmpty()) nome = null;
        if (cidade != null && cidade.isEmpty()) cidade = null;
        if (estado != null && estado.isEmpty()) estado = null;

        List<Contatos> contatos = contatosRepositorio.filtrarRelatorio(nome, cidade, estado, grupoId, usuarioLogado.getEmpresa());
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
    
    // =========================================================================
    // NOVO MÉTODO: GERAÇÃO DE ETIQUETAS (NA RAÇA!)
    // =========================================================================
    @GetMapping("/relatorio/etiquetas")
    public ResponseEntity<InputStreamResource> etiquetasContatosPdf(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "cidade", required = false) String cidade,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "grupoId", required = false) Long grupoId,
            @RequestParam(value = "dataInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(value = "dataFim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            
            // <-- NOSSOS NOVOS CAMPOS CAPTURADOS DO FRONT-END -->
            @RequestParam(value = "modeloEtiqueta", defaultValue = "6180") String modeloEtiqueta,
            @RequestParam(value = "posicaoInicial", defaultValue = "1") Integer posicaoInicial,
            
            @AuthenticationPrincipal UsuarioLogado usuarioLogado) { 
        
        if (nome != null && nome.isEmpty()) nome = null;
        if (cidade != null && cidade.isEmpty()) cidade = null;
        if (estado != null && estado.isEmpty()) estado = null;

        List<Contatos> contatos = contatosRepositorio.filtrarRelatorio(nome, cidade, estado, grupoId, usuarioLogado.getEmpresa());
        contatos = filtrarPorPeriodoCadastro(contatos, dataInicio, dataFim);
        
        // <-- CHAMA O NOVO SERVIÇO PASSANDO OS PARÂMETROS -->
        ByteArrayInputStream bis = etiquetaService.gerarPdfEtiquetas(contatos, modeloEtiqueta, posicaoInicial);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=etiquetas_contatos.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}