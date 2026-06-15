package com.projeto.sistema.controle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal; 
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.projeto.sistema.modelos.Contatos;
import com.projeto.sistema.modelos.Lembrete;
import com.projeto.sistema.modelos.LembreteDTO;
import com.projeto.sistema.modelos.UsuarioLogado;
import com.projeto.sistema.modelos.UF; 
import com.projeto.sistema.modelos.Grupo; 
import com.projeto.sistema.modelos.MensagemLog; // <-- IMPORT NECESSÁRIO
import com.projeto.sistema.repositorios.ContatosRepositorio;
import com.projeto.sistema.repositorios.GrupoRepositorio;
import com.projeto.sistema.repositorios.LembreteRepositorio;
import com.projeto.sistema.repositorios.MensagemLogRepositorio; // <-- IMPORT NECESSÁRIO

@ControllerAdvice
public class GlobalAtributos {

	@Autowired
	private ContatosRepositorio contatosRepositorio;

	@Autowired
	private LembreteRepositorio lembreteRepositorio;

	@Autowired
	private GrupoRepositorio grupoRepositorio;

    // --- REPOSITÓRIO DE MENSAGENS INJETADO ---
	@Autowired
	private MensagemLogRepositorio mensagemLogRepositorio; 

	@ModelAttribute("listaEstados")
	public UF[] getListaEstados() {
		return UF.values();
	}

	@ModelAttribute("listaGrupos") 
	public List<Grupo> carregarGruposGlobais(@AuthenticationPrincipal UsuarioLogado usuarioLogado) {
		if (usuarioLogado != null && usuarioLogado.getEmpresa() != null) {
			return grupoRepositorio.findByEmpresa(usuarioLogado.getEmpresa());
		}
		return new ArrayList<>();
	}

	@ModelAttribute("listaLembretes")
	public List<LembreteDTO> carregarLembretesFuturos(@AuthenticationPrincipal UsuarioLogado usuarioLogado) { 
		List<LembreteDTO> lembretes = new ArrayList<>();

		if (usuarioLogado == null) {
			return lembretes;
		}

		LocalDate amanha = LocalDate.now().plusDays(1);
		String diaMesAmanha = String.format("%02d/%02d", amanha.getDayOfMonth(), amanha.getMonthValue());

		List<Contatos> aniversariantesAmanha = contatosRepositorio.findByDiaEMesAniversario(diaMesAmanha,
				usuarioLogado.getEmpresa());

		for (Contatos c : aniversariantesAmanha) {
			lembretes.add(new LembreteDTO("Aniversário Amanhã", "Não esqueça de parabenizar " + c.getNome(), "NIVER",
					amanha, c.getId()));
		}

		LocalDateTime inicioDia = amanha.atStartOfDay();
		LocalDateTime fimDia = amanha.atTime(LocalTime.MAX);

		List<Lembrete> tarefasAmanha = lembreteRepositorio.findByDataHoraBetweenAndEmpresa(inicioDia, fimDia,
				usuarioLogado.getEmpresa());

		for (Lembrete l : tarefasAmanha) {
			Long idReferencia = (l.getContato() != null) ? l.getContato().getId() : 0L;
			lembretes.add(new LembreteDTO(l.getTitulo(), l.getDescricao() != null ? l.getDescricao() : "Sem descrição",
					l.getTipo(), amanha, idReferencia));
		}
		return lembretes;
	}

	@ModelAttribute("qtdLembretes")
	public int contarLembretes(@AuthenticationPrincipal UsuarioLogado usuarioLogado) { 
		return carregarLembretesFuturos(usuarioLogado).size(); 
	}

    // --- NOVA PARTE: NOTIFICAÇÕES DINÂMICAS ---
    
	@ModelAttribute("qtdNotificacoes")
	public int contarNotificacoesNaoLidas(@AuthenticationPrincipal UsuarioLogado usuarioLogado) {
		if (usuarioLogado != null && usuarioLogado.getEmpresa() != null) {
            // Fazemos o cast (int) porque o count do Spring Data retorna um 'long'
            // O contador reflete rigorosamente apenas as mensagens não lidas
			return (int) mensagemLogRepositorio.countByLidaFalseAndEmpresa(usuarioLogado.getEmpresa());
		}
		return 0;
	}

	@ModelAttribute("listaNotificacoes")
	public List<MensagemLog> carregarNotificacoesNaoLidas(@AuthenticationPrincipal UsuarioLogado usuarioLogado) {
		if (usuarioLogado != null && usuarioLogado.getEmpresa() != null) {
            // Utilizamos o método que já traz o Top 5 ordenado pela data mais recente
			return mensagemLogRepositorio.findTop5ByLidaFalseAndEmpresaOrderByDataEnvioDesc(usuarioLogado.getEmpresa());
		}
		return new ArrayList<>();
	}
}