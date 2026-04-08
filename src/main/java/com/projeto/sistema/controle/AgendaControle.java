package com.projeto.sistema.controle;

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
import org.springframework.security.core.annotation.AuthenticationPrincipal; // <-- IMPORT DO CRACHÁ
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
import com.projeto.sistema.modelos.UsuarioLogado; // <-- IMPORT DO CRACHÁ
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

	// FECHADURA: Apenas quem pode VER a agenda
	@PreAuthorize("hasAuthority('AGENDA_VISUALIZAR')")
	@GetMapping("/administrativo/agenda")
	public ModelAndView acessarAgenda(@RequestParam(required = false) Integer mes,
			@RequestParam(required = false) Integer ano, @AuthenticationPrincipal UsuarioLogado usuarioLogado) { // <-- RECEBE O CRACHÁ

		ModelAndView mv = new ModelAndView("administrativo/agenda");

		LocalDate hoje = LocalDate.now();
		int mesAtual = (mes != null) ? mes : hoje.getMonthValue();
		int anoAtual = (ano != null) ? ano : hoje.getYear();

		YearMonth anoMes = YearMonth.of(anoAtual, mesAtual);
		LocalDateTime inicioMes = anoMes.atDay(1).atStartOfDay();
		LocalDateTime fimMes = anoMes.atEndOfMonth().atTime(LocalTime.MAX);

		String mesFormatadoBanco = String.format("/%02d", mesAtual);

		// <-- PASSA A EMPRESA NA BUSCA DOS ANIVERSARIANTES
		List<Contatos> aniversariantes = contatosRepositorio.findByMesAniversario(mesFormatadoBanco, usuarioLogado.getEmpresa());

		// <-- PASSA A EMPRESA NA BUSCA DE MENSAGENS 
		List<MensagemLog> envios = mensagemRepositorio.findByDataEnvioBetweenAndEmpresa(inicioMes, fimMes, usuarioLogado.getEmpresa());

		// <-- PASSA A EMPRESA NA BUSCA DE LEMBRETES 
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
						LocalDate dataNiver = LocalDate.of(anoAtual, mesAtual, diaNiver);
						eventos.add(new EventoAgenda(dataNiver, "NIVER", c.getNome(), "event-niver"));
					} catch (DateTimeException e) {
						// Ignora
					}
				} catch (NumberFormatException e) {
					// Ignora
				}
			}
		}

		// 2. PROCESSAR ENVIOS
		for (MensagemLog log : envios) {
			eventos.add(new EventoAgenda(log.getDataEnvio().toLocalDate(), "ENVIO", log.getAssunto(), "event-envio"));
		}

		// 3. PROCESSAR LEMBRETES
		for (Lembrete l : lembretes) {
			String corClasse = "event-tarefa";
			if ("REUNIAO".equalsIgnoreCase(l.getTipo()))
				corClasse = "event-reuniao";
			else if ("IMPORTANTE".equalsIgnoreCase(l.getTipo()))
				corClasse = "event-importante";

			eventos.add(new EventoAgenda(l.getDataHora().toLocalDate(), l.getTipo(), l.getTitulo(), corClasse));
		}

		eventos.sort(Comparator.comparing(EventoAgenda::getData));

		mv.addObject("listaEventos", eventos);
		mv.addObject("paginaAtiva", "agenda");
		mv.addObject("mesExibicao", mesAtual);
		mv.addObject("anoExibicao", anoAtual);
		mv.addObject("totalDiasMes", anoMes.lengthOfMonth());

		// <-- PASSA A EMPRESA PARA LISTAR APENAS OS CONTATOS DO UTILIZADOR NO MODAL DE NOVO LEMBRETE
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
		lembrete.setEmpresa(usuarioLogado.getEmpresa()); // <-- CARIMBADO COM SUCESSO
		lembreteRepositorio.save(lembrete);
		return "redirect:/administrativo/agenda";
	}

	// FECHADURA: Apenas quem pode EXCLUIR na agenda
	@PreAuthorize("hasAuthority('AGENDA_EXCLUIR')")
	@GetMapping("/administrativo/agenda/remover/{id}")
	public ResponseEntity<?> removerEvento(@PathVariable Long id, @AuthenticationPrincipal UsuarioLogado usuarioLogado) { // <-- RECEBE CRACHÁ
		try {
			Lembrete lembrete = lembreteRepositorio.findById(id).orElse(null);
			// SEGURANÇA: Só apaga se existir e for da empresa do utilizador logado
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
	public Lembrete buscarEventoParaEdicao(@PathVariable Long id, @AuthenticationPrincipal UsuarioLogado usuarioLogado) { // <-- RECEBE CRACHÁ
		Lembrete lembrete = lembreteRepositorio.findById(id).orElse(null);
		// SEGURANÇA: Só devolve os dados se o lembrete for da empresa do utilizador
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
			@AuthenticationPrincipal UsuarioLogado usuarioLogado) { // <-- RECEBE O CRACHÁ

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

		// 3. Lembretes (CORRIGIDO AQUI: Adicionado o AndEmpresa e o crachá)
		List<Lembrete> lembretesDia = lembreteRepositorio.findByDataHoraBetweenAndEmpresa(inicio, fim, usuarioLogado.getEmpresa());
		for (Lembrete l : lembretesDia) {
			String subtitulo = (l.getContato() != null) ? "Com: " + l.getContato().getNome() : l.getDescricao();
			detalhes.add(new DetalheAgendaDTO(l.getTipo(), l.getTitulo(), subtitulo, l.getId()));
		}

		// 4. Feriados
		List<EventoAgenda> feriados = getFeriadosDoMes(data.getMonthValue(), data.getYear());
		for (EventoAgenda f : feriados) {
			if (f.getData().isEqual(data)) {
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
				feriados.add(new EventoAgenda(LocalDate.of(ano, mes, datasFixas[i][0]), "FERIADO", nomesFixos[i],
						"event-feriado"));
			}
		}
		return feriados;
	}
}