package com.projeto.sistema.controle;

import java.io.ByteArrayInputStream;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.projeto.sistema.modelos.Contatos;
import com.projeto.sistema.modelos.DetalheAgendaDTO;
import com.projeto.sistema.modelos.EventoAgenda;
import com.projeto.sistema.modelos.Lembrete;
import com.projeto.sistema.modelos.MensagemLog;
import com.projeto.sistema.modelos.UsuarioLogado;
import com.projeto.sistema.repositorios.ContatosRepositorio;
import com.projeto.sistema.repositorios.LembreteRepositorio;
import com.projeto.sistema.repositorios.MensagemLogRepositorio;

@Controller
public class AgendaControle {

	@Autowired
	private ContatosRepositorio contatosRepositorio;

	@Autowired
	private MensagemLogRepositorio mensagemRepositorio;

	@Autowired
	private LembreteRepositorio lembreteRepositorio;
	
	@Autowired
	private com.projeto.sistema.servicos.RelatorioService relatorioService;

	// FECHADURA: Apenas quem pode VER a agenda
	@PreAuthorize("hasAuthority('AGENDA_VISUALIZAR')")
	@GetMapping("/administrativo/agenda")
	public ModelAndView acessarAgenda(@RequestParam(required = false) Integer mes,
			@RequestParam(required = false) Integer ano, @AuthenticationPrincipal UsuarioLogado usuarioLogado) { 

		ModelAndView mv = new ModelAndView("administrativo/agenda");

		LocalDate hoje = LocalDate.now();
		int mesAtual = (mes != null) ? mes : hoje.getMonthValue();
		int anoAtual = (ano != null) ? ano : hoje.getYear();

		YearMonth anoMes = YearMonth.of(anoAtual, mesAtual);
		LocalDateTime inicioMes = anoMes.atDay(1).atStartOfDay();
		LocalDateTime fimMes = anoMes.atEndOfMonth().atTime(LocalTime.MAX);

		String mesFormatadoBanco = String.format("/%02d", mesAtual);

		List<Contatos> aniversariantes = contatosRepositorio.findByMesAniversario(mesFormatadoBanco, usuarioLogado.getEmpresa());
		List<MensagemLog> envios = mensagemRepositorio.findByDataEnvioBetweenAndEmpresa(inicioMes, fimMes, usuarioLogado.getEmpresa());
		List<Lembrete> lembretes = lembreteRepositorio.findByDataHoraBetweenAndEmpresa(inicioMes, fimMes, usuarioLogado.getEmpresa());

		List<EventoAgenda> eventos = new ArrayList<>();
		eventos.addAll(getFeriadosDoMes(mesAtual, anoAtual));

		// 1. PROCESSAR ANIVERSARIANTES
		for (Contatos c : aniversariantes) {
			if (!c.getExibirNaAgenda()) {
				continue;
			}

			if (c.getDataNascimento() != null && c.getDataNascimento().length() >= 5) {
				try {
					int diaNiver = Integer.parseInt(c.getDataNascimento().substring(0, 2));
					try {
						// Niver não tem hora, forçamos meia noite (atStartOfDay)
						LocalDate dataNiver = LocalDate.of(anoAtual, mesAtual, diaNiver);
						eventos.add(new EventoAgenda(dataNiver.atStartOfDay(), "NIVER", c.getNome(), "event-niver"));
					} catch (DateTimeException e) {}
				} catch (NumberFormatException e) {}
			}
		}

		// 2. PROCESSAR ENVIOS (Mantém a HORA agora)
		for (MensagemLog log : envios) {
			eventos.add(new EventoAgenda(log.getDataEnvio(), "ENVIO", log.getAssunto(), "event-envio"));
		}

		// 3. PROCESSAR LEMBRETES (Mantém a HORA agora)
		for (Lembrete l : lembretes) {
			String corClasse = "event-tarefa";
			if ("REUNIAO".equalsIgnoreCase(l.getTipo()))
				corClasse = "event-reuniao";
			else if ("IMPORTANTE".equalsIgnoreCase(l.getTipo()))
				corClasse = "event-importante";

			eventos.add(new EventoAgenda(l.getDataHora(), l.getTipo(), l.getTitulo(), corClasse));
		}

		eventos.sort(Comparator.comparing(EventoAgenda::getData));

		mv.addObject("listaEventos", eventos);
		mv.addObject("paginaAtiva", "agenda");
		mv.addObject("mesExibicao", mesAtual);
		mv.addObject("anoExibicao", anoAtual);
		mv.addObject("totalDiasMes", anoMes.lengthOfMonth());
		mv.addObject("todosContatos", contatosRepositorio.findByEmpresa(usuarioLogado.getEmpresa()));
		mv.addObject("novoLembrete", new Lembrete());

		int diaSemanaPrimeiroDia = anoMes.atDay(1).getDayOfWeek().getValue();
		mv.addObject("diaSemanaInicio", diaSemanaPrimeiroDia);

		return mv;
	}

	// FECHADURA: Apenas quem pode CRIAR ou EDITAR na agenda
	@PreAuthorize("hasAnyAuthority('AGENDA_CRIAR', 'AGENDA_EDITAR')")
	@PostMapping("/administrativo/agenda/salvar")
	public String salvarLembrete(Lembrete lembrete, @AuthenticationPrincipal UsuarioLogado usuarioLogado) {
		lembrete.setEmpresa(usuarioLogado.getEmpresa());
		lembreteRepositorio.save(lembrete);
		return "redirect:/administrativo/agenda";
	}

	// FECHADURA: Apenas quem pode EXCLUIR na agenda
	@PreAuthorize("hasAuthority('AGENDA_EXCLUIR')")
	@GetMapping("/administrativo/agenda/remover/{id}")
	public ResponseEntity<?> removerEvento(@PathVariable Long id, @AuthenticationPrincipal UsuarioLogado usuarioLogado) {
		try {
			Lembrete lembrete = lembreteRepositorio.findById(id).orElse(null);
			if (lembrete != null && lembrete.getEmpresa().getId().equals(usuarioLogado.getEmpresa().getId())) {
				lembreteRepositorio.deleteById(id);
				return ResponseEntity.ok().body("Evento excluído com sucesso.");
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Evento não encontrado ou sem permissão.");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao excluir: " + e.getMessage());
		}
	}

	// FECHADURA: Quem pode VER a agenda também precisa poder ver os detalhes para edição
	@PreAuthorize("hasAuthority('AGENDA_VISUALIZAR')")
	@GetMapping("/administrativo/agenda/buscar/{id}")
	@ResponseBody
	public Lembrete buscarEventoParaEdicao(@PathVariable Long id, @AuthenticationPrincipal UsuarioLogado usuarioLogado) {
		Lembrete lembrete = lembreteRepositorio.findById(id).orElse(null);
		if (lembrete != null && lembrete.getEmpresa().getId().equals(usuarioLogado.getEmpresa().getId())) {
			return lembrete;
		}
		return null;
	}

	// FECHADURA: Apenas quem pode VER a agenda pode clicar num dia e ver os detalhes
	@PreAuthorize("hasAuthority('AGENDA_VISUALIZAR')")
	@GetMapping("/administrativo/agenda/detalhes")
	@ResponseBody
	public List<DetalheAgendaDTO> obterDetalhesDia(
			@RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
			@AuthenticationPrincipal UsuarioLogado usuarioLogado) { 

		List<DetalheAgendaDTO> detalhes = new ArrayList<>();

		// 1. Aniversários
		String diaMesFormatado = String.format("%02d/%02d", data.getDayOfMonth(), data.getMonthValue());
		List<Contatos> nivers = contatosRepositorio.findByDiaEMesAniversario(diaMesFormatado, usuarioLogado.getEmpresa());

		for (Contatos c : nivers) {
			detalhes.add(new DetalheAgendaDTO("NIVER", c.getNome(), c.getEmail(), c.getId()));
		}

		// 2. Envios
		LocalDateTime inicio = data.atStartOfDay();
		LocalDateTime fim = data.atTime(LocalTime.MAX);

		List<MensagemLog> msgs = mensagemRepositorio.findByDataEnvioBetweenAndEmpresa(inicio, fim, usuarioLogado.getEmpresa());

		for (MensagemLog m : msgs) {
			String horaFormatada = m.getDataEnvio().toLocalTime().toString();
			if (horaFormatada.length() > 5) {
				horaFormatada = horaFormatada.substring(0, 5);
			}

			String nomeGrupo = (m.getNomeGrupoDestino() != null) ? m.getNomeGrupoDestino() : "Sem grupo";
			String subtitulo = horaFormatada + " - " + nomeGrupo;

			detalhes.add(new DetalheAgendaDTO("ENVIO", m.getAssunto(), subtitulo, m.getId()));
		}

		// 3. Lembretes 
		List<Lembrete> lembretesDia = lembreteRepositorio.findByDataHoraBetweenAndEmpresa(inicio, fim, usuarioLogado.getEmpresa());
		for (Lembrete l : lembretesDia) {
			String subtitulo = (l.getContato() != null) ? "Com: " + l.getContato().getNome() : l.getDescricao();
			detalhes.add(new DetalheAgendaDTO(l.getTipo(), l.getTitulo(), subtitulo, l.getId()));
		}

		// 4. Feriados
		List<EventoAgenda> feriados = getFeriadosDoMes(data.getMonthValue(), data.getYear());
		for (EventoAgenda f : feriados) {
			if (f.getData().toLocalDate().isEqual(data)) {
				detalhes.add(new DetalheAgendaDTO("FERIADO", f.getTitulo(), "Feriado Nacional", null));
			}
		}

		return detalhes;
	}

	private List<EventoAgenda> getFeriadosDoMes(int mes, int ano) {
		List<EventoAgenda> feriados = new ArrayList<>();
		int[][] datasFixas = { { 1, 1 }, { 21, 4 }, { 1, 5 }, { 7, 9 }, { 12, 10 }, { 2, 11 }, { 15, 11 }, { 25, 12 } };
		String[] nomesFixos = { "Ano Novo", "Tiradentes", "Dia do Trabalho", "Independência", "N. Sra. Aparecida",
				"Finados", "Proc. República", "Natal" };
		for (int i = 0; i < datasFixas.length; i++) {
			if (datasFixas[i][1] == mes) {
				// Adiciona os feriados com 00:00 de hora
				feriados.add(new EventoAgenda(LocalDate.of(ano, mes, datasFixas[i][0]).atStartOfDay(), "FERIADO", nomesFixos[i],
						"event-feriado"));
			}
		}
		return feriados;
	}
	
	// FECHADURA: Apenas quem pode VER a agenda pode exportar
	@PreAuthorize("hasAuthority('AGENDA_VISUALIZAR')")
	@GetMapping("/administrativo/agenda/exportar")
	public ResponseEntity<org.springframework.core.io.InputStreamResource> exportarAgendaPdf(
			@RequestParam(required = false) Integer mes,
			@RequestParam(required = false) Integer ano, 
			@AuthenticationPrincipal UsuarioLogado usuarioLogado) {

		// 1. Descobrir de qual mês estamos falando
		LocalDate hoje = LocalDate.now();
		int mesAtual = (mes != null) ? mes : hoje.getMonthValue();
		int anoAtual = (ano != null) ? ano : hoje.getYear();

		YearMonth anoMes = YearMonth.of(anoAtual, mesAtual);
		LocalDateTime inicioMes = anoMes.atDay(1).atStartOfDay();
		LocalDateTime fimMes = anoMes.atEndOfMonth().atTime(LocalTime.MAX);
		String mesFormatadoBanco = String.format("/%02d", mesAtual);

		// 2. Buscar TUDO no banco (igual a tela faz)
		List<Contatos> aniversariantes = contatosRepositorio.findByMesAniversario(mesFormatadoBanco, usuarioLogado.getEmpresa());
		List<MensagemLog> envios = mensagemRepositorio.findByDataEnvioBetweenAndEmpresa(inicioMes, fimMes, usuarioLogado.getEmpresa());
		List<Lembrete> lembretes = lembreteRepositorio.findByDataHoraBetweenAndEmpresa(inicioMes, fimMes, usuarioLogado.getEmpresa());

		// 3. Juntar tudo na mesma lista e organizar
		List<EventoAgenda> eventos = new ArrayList<>();
		eventos.addAll(getFeriadosDoMes(mesAtual, anoAtual));

		for (Contatos c : aniversariantes) {
			if (c.getExibirNaAgenda() && c.getDataNascimento() != null && c.getDataNascimento().length() >= 5) {
				try {
					int diaNiver = Integer.parseInt(c.getDataNascimento().substring(0, 2));
					eventos.add(new EventoAgenda(LocalDate.of(anoAtual, mesAtual, diaNiver).atStartOfDay(), "NIVER", c.getNome(), "event-niver"));
				} catch (Exception e) {}
			}
		}
		for (MensagemLog log : envios) {
			eventos.add(new EventoAgenda(log.getDataEnvio(), "ENVIO", log.getAssunto(), "event-envio"));
		}
		for (Lembrete l : lembretes) {
			eventos.add(new EventoAgenda(l.getDataHora(), l.getTipo(), l.getTitulo(), "event-tarefa"));
		}

		// Ordena os eventos por data para o PDF ficar certinho cronologicamente
		eventos.sort(Comparator.comparing(EventoAgenda::getData));

		// 4. Gerar o PDF!
		String mesAnoStr = String.format("%02d/%d", mesAtual, anoAtual);
		ByteArrayInputStream bis = relatorioService.gerarRelatorioAgenda(eventos, mesAnoStr);

		org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
		// "inline" abre no navegador. Se quiser que baixe direto pro PC, troque para "attachment"
		headers.add("Content-Disposition", "inline; filename=produtividade_" + mesAtual + "_" + anoAtual + ".pdf");

		return ResponseEntity
				.ok()
				.headers(headers)
				.contentType(org.springframework.http.MediaType.APPLICATION_PDF)
				.body(new org.springframework.core.io.InputStreamResource(bis));
	}
}