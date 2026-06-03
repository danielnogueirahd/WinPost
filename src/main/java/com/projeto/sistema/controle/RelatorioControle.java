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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.projeto.sistema.modelos.Contatos;
import com.projeto.sistema.modelos.EventoAgenda;
import com.projeto.sistema.modelos.UsuarioLogado;
import com.projeto.sistema.repositorios.ContatosRepositorio;
import com.projeto.sistema.servicos.RelatorioService;
import com.projeto.sistema.servicos.EtiquetaService; // NOVO: Importar o serviço correto!

@Controller
@RequestMapping("/relatorio")
public class RelatorioControle {

	@Autowired
	private RelatorioService relatorioService;

	// NOVO: Trazer o teu serviço especialista em etiquetas para o controlador
	@Autowired
	private EtiquetaService etiquetaService;

	@Autowired
	private ContatosRepositorio contatosRepositorio;

	// =========================================================================
	// 1. EXPORTAR LISTA DE CONTATOS (PDF)
	// =========================================================================
	@RequestMapping(value = "/pdf", method = { RequestMethod.GET, RequestMethod.POST })
	public ResponseEntity<InputStreamResource> exportarContatosPdf(
			@RequestParam(value = "idsSelecionados", required = false) List<Long> idsSelecionados,
			@RequestParam(value = "nome", required = false) String nome,
			@RequestParam(value = "cidade", required = false) String cidade,
			@RequestParam(value = "estado", required = false) String estado,
			@RequestParam(value = "grupoId", required = false) Long grupoId,
			@AuthenticationPrincipal UsuarioLogado usuarioLogado) {

		// DICA: Transformando textos vazios do formulário em 'null' para a Query do
		// banco funcionar certinho
		if (nome != null && nome.trim().isEmpty())
			nome = null;
		if (cidade != null && cidade.trim().isEmpty())
			cidade = null;
		if (estado != null && estado.trim().isEmpty())
			estado = null;

		List<Contatos> contatosParaExportar;

		if (idsSelecionados != null && !idsSelecionados.isEmpty()) {
			contatosParaExportar = contatosRepositorio.findAllById(idsSelecionados);
		} else {
			contatosParaExportar = contatosRepositorio.filtrarRelatorio(nome, cidade, estado, grupoId,
					usuarioLogado.getEmpresa());
		}

		ByteArrayInputStream bis = relatorioService.gerarRelatorioContatos(contatosParaExportar);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "inline; filename=relatorio_contatos.pdf");

		return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF)
				.body(new InputStreamResource(bis));
	}

	// =========================================================================
	// 2. EXPORTAR ETIQUETAS
	// =========================================================================
	@RequestMapping(value = "/etiquetas", method = { RequestMethod.GET, RequestMethod.POST })
	public ResponseEntity<InputStreamResource> exportarEtiquetas(
			@RequestParam(value = "idsSelecionados", required = false) List<Long> idsSelecionados,
			@RequestParam(value = "nome", required = false) String nome,
			@RequestParam(value = "cidade", required = false) String cidade,
			@RequestParam(value = "estado", required = false) String estado,
			@RequestParam(value = "grupoId", required = false) Long grupoId,
			@RequestParam(value = "modeloEtiqueta", defaultValue = "6180") String modeloEtiqueta,
			@RequestParam(value = "posicaoInicial", defaultValue = "1") int posicaoInicial,
			@AuthenticationPrincipal UsuarioLogado usuarioLogado) {

		// Mesma limpeza de variáveis para as etiquetas
		if (nome != null && nome.trim().isEmpty())
			nome = null;
		if (cidade != null && cidade.trim().isEmpty())
			cidade = null;
		if (estado != null && estado.trim().isEmpty())
			estado = null;

		List<Contatos> contatosParaExportar;

		if (idsSelecionados != null && !idsSelecionados.isEmpty()) {
			contatosParaExportar = contatosRepositorio.findAllById(idsSelecionados);
		} else {
			contatosParaExportar = contatosRepositorio.filtrarRelatorio(nome, cidade, estado, grupoId,
					usuarioLogado.getEmpresa());
		}

		ByteArrayInputStream bis = etiquetaService.gerarPdfEtiquetas(contatosParaExportar, modeloEtiqueta,
				posicaoInicial);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "inline; filename=etiquetas_correios.pdf");

		return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF)
				.body(new InputStreamResource(bis));
	}

}