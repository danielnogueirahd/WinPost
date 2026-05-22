package com.projeto.sistema.controle;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.projeto.sistema.modelos.Contatos;
import com.projeto.sistema.modelos.EventoAgenda;
import com.projeto.sistema.modelos.UsuarioLogado;
import com.projeto.sistema.repositorios.ContatosRepositorio;
import com.projeto.sistema.repositorios.AgendaRepositorio; // Ajuste para o nome correto do seu repositório
import com.projeto.sistema.servicos.RelatorioService;

@Controller
@RequestMapping("/relatorio")
public class RelatorioControle {

    @Autowired
    private RelatorioService relatorioService;

    @Autowired
    private ContatosRepositorio contatosRepositorio;
    
    // Adicione o repositório da agenda se ainda não tiver
    // @Autowired
    // private AgendaRepositorio agendaRepositorio;

    // =========================================================================
    // 1. EXPORTAR LISTA DE CONTATOS (PDF)
    // =========================================================================
    @PostMapping("/pdf")
    public ResponseEntity<InputStreamResource> exportarContatosPdf(
            @RequestParam(value = "idsSelecionados", required = false) List<Long> idsSelecionados,
            @AuthenticationPrincipal UsuarioLogado usuarioLogado) {

        List<Contatos> contatosParaExportar;

        // INTELIGÊNCIA: Se não veio ID nenhum (ex: clicou pelo Navbar global), busca todos da empresa!
        if (idsSelecionados == null || idsSelecionados.isEmpty()) {
            contatosParaExportar = contatosRepositorio.findByEmpresa(usuarioLogado.getEmpresa());
        } else {
            // Se vieram IDs (ex: selecionou na tabela), busca só aqueles
            contatosParaExportar = contatosRepositorio.findAllById(idsSelecionados);
        }

        // Chama o seu serviço para gerar o PDF
        ByteArrayInputStream bis = relatorioService.gerarRelatorioContatos(contatosParaExportar);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=relatorio_contatos.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    // =========================================================================
    // 2. EXPORTAR ETIQUETAS
    // =========================================================================
    @PostMapping("/etiquetas")
    public ResponseEntity<InputStreamResource> exportarEtiquetas(
            @RequestParam(value = "idsSelecionados", required = false) List<Long> idsSelecionados,
            @AuthenticationPrincipal UsuarioLogado usuarioLogado) {

        List<Contatos> contatosParaExportar;

        if (idsSelecionados == null || idsSelecionados.isEmpty()) {
            contatosParaExportar = contatosRepositorio.findByEmpresa(usuarioLogado.getEmpresa());
        } else {
            contatosParaExportar = contatosRepositorio.findAllById(idsSelecionados);
        }

        ByteArrayInputStream bis = relatorioService.gerarEtiquetas(contatosParaExportar);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=etiquetas_correios.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    // =========================================================================
    // 3. EXPORTAR PRODUTIVIDADE DA AGENDA (MÊS)
    // =========================================================================
    @GetMapping("/agenda/pdf")
    public ResponseEntity<InputStreamResource> exportarAgendaPdf(
            @RequestParam(value = "mesAno", defaultValue = "Mês Atual") String mesAno,
            @AuthenticationPrincipal UsuarioLogado usuarioLogado) {

        // DICA: Como a sua Agenda depende do mês selecionado lá no modal do Navbar, 
        // aqui você recebe a String "Janeiro", "Fevereiro", ou "Mês Atual".
        // Você vai usar esse parâmetro para fazer um `if` ou `switch` e buscar os eventos corretos no banco.

        /* EXEMPLO DE BUSCA NO BANCO:
        List<EventoAgenda> eventos;
        if (mesAno.equals("Mês Atual")) {
             eventos = agendaRepositorio.findByEmpresaAndMesAtual(usuarioLogado.getEmpresa());
        } else {
             int numeroMes = converterNomeMesParaNumero(mesAno);
             eventos = agendaRepositorio.findByEmpresaAndMes(usuarioLogado.getEmpresa(), numeroMes);
        }
        */

        // Para não quebrar o seu código atual, estou passando null, mas você deve substituir pela lista de eventos real do banco:
        List<EventoAgenda> eventosDaEmpresa = null; 

        // Chama o seu serviço que gera o layout do PDF da agenda
        ByteArrayInputStream bis = relatorioService.gerarRelatorioAgenda(eventosDaEmpresa, mesAno);

        HttpHeaders headers = new HttpHeaders();
        // O "inline" faz o PDF abrir no navegador em vez de forçar o download direto
        headers.add("Content-Disposition", "inline; filename=relatorio_agenda_" + mesAno.toLowerCase() + ".pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}