package com.projeto.sistema.controle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.projeto.sistema.modelos.Contatos;
import com.projeto.sistema.modelos.DetalheAgendaDTO;
import com.projeto.sistema.modelos.EventoAgenda;
import com.projeto.sistema.modelos.Lembrete;
import com.projeto.sistema.modelos.MensagemLog;
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

    // 1. ALTERADO: Rota agora é /administrativo/agenda
    @GetMapping("/administrativo/agenda")
    public ModelAndView acessarAgenda(@RequestParam(required = false) Integer mes, 
                                      @RequestParam(required = false) Integer ano) {
        
        ModelAndView mv = new ModelAndView("administrativo/agenda");
        
        LocalDate hoje = LocalDate.now();
        int mesAtual = (mes != null) ? mes : hoje.getMonthValue();
        int anoAtual = (ano != null) ? ano : hoje.getYear();
        
        YearMonth anoMes = YearMonth.of(anoAtual, mesAtual);
        LocalDateTime inicioMes = anoMes.atDay(1).atStartOfDay();
        LocalDateTime fimMes = anoMes.atEndOfMonth().atTime(LocalTime.MAX);

        List<Contatos> aniversariantes = contatosRepositorio.findByMesAniversario(mesAtual);
        List<MensagemLog> envios = mensagemRepositorio.findByDataEnvioBetween(inicioMes, fimMes);
        List<Lembrete> lembretes = lembreteRepositorio.findByDataHoraBetween(inicioMes, fimMes);

        List<EventoAgenda> eventos = new ArrayList<>();
        eventos.addAll(getFeriadosDoMes(mesAtual, anoAtual));

        for (Contatos c : aniversariantes) {
            LocalDate dataNiver = LocalDate.of(anoAtual, mesAtual, c.getDataNascimento().getDayOfMonth());
            eventos.add(new EventoAgenda(dataNiver, "NIVER", c.getNome(), "event-niver"));
        }

        for (MensagemLog log : envios) {
            eventos.add(new EventoAgenda(log.getDataEnvio().toLocalDate(), "ENVIO", log.getAssunto(), "event-envio"));
        }

        for (Lembrete l : lembretes) {
            String corClasse = "event-tarefa";
            if ("REUNIAO".equalsIgnoreCase(l.getTipo())) corClasse = "event-reuniao";
            else if ("IMPORTANTE".equalsIgnoreCase(l.getTipo())) corClasse = "event-importante";
            
            eventos.add(new EventoAgenda(l.getDataHora().toLocalDate(), l.getTipo(), l.getTitulo(), corClasse));
        }

        eventos.sort(Comparator.comparing(EventoAgenda::getData));

        mv.addObject("listaEventos", eventos);
        mv.addObject("paginaAtiva", "agenda");
        mv.addObject("mesExibicao", mesAtual);
        mv.addObject("anoExibicao", anoAtual);
        mv.addObject("totalDiasMes", anoMes.lengthOfMonth());
        
        mv.addObject("todosContatos", contatosRepositorio.findAll());
        mv.addObject("novoLembrete", new Lembrete()); 
        
        int diaSemanaPrimeiroDia = anoMes.atDay(1).getDayOfWeek().getValue();
        mv.addObject("diaSemanaInicio", diaSemanaPrimeiroDia); 
        
        return mv;
    }

    // 2. ALTERADO: Rota de salvar ajustada
    @PostMapping("/administrativo/agenda/salvar")
    public String salvarLembrete(Lembrete lembrete) {
        lembreteRepositorio.save(lembrete);
        // 3. ALTERADO: Redireciona para o caminho correto
        return "redirect:/administrativo/agenda";
    }

    // 4. ALTERADO: Rota da API AJAX ajustada
    @GetMapping("/administrativo/agenda/detalhes")
    @ResponseBody 
    public List<DetalheAgendaDTO> obterDetalhesDia(
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        
        List<DetalheAgendaDTO> detalhes = new ArrayList<>();

        List<Contatos> nivers = contatosRepositorio.findByDiaEMesAniversario(data.getDayOfMonth(), data.getMonthValue());
        for (Contatos c : nivers) {
            detalhes.add(new DetalheAgendaDTO("NIVER", c.getNome(), c.getEmail(), c.getId()));
        }

        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.atTime(LocalTime.MAX);
        
        List<MensagemLog> msgs = mensagemRepositorio.findByDataEnvioBetween(inicio, fim);
        for (MensagemLog m : msgs) {
            detalhes.add(new DetalheAgendaDTO("ENVIO", m.getAssunto(), "Grupo: " + m.getNomeGrupoDestino(), m.getId()));
        }
        
        List<Lembrete> lembretesDia = lembreteRepositorio.findByDataHoraBetween(inicio, fim);
        for (Lembrete l : lembretesDia) {
            String subtitulo = l.getDescricao();
            if(l.getContato() != null) {
                subtitulo = "Com: " + l.getContato().getNome(); 
            }
            detalhes.add(new DetalheAgendaDTO(l.getTipo(), l.getTitulo(), subtitulo, l.getId()));
        }

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
        int[][] datasFixas = {
            {1, 1}, {21, 4}, {1, 5}, {7, 9}, {12, 10}, {2, 11}, {15, 11}, {25, 12}
        };
        String[] nomesFixos = {
            "Ano Novo", "Tiradentes", "Dia do Trabalho", "Independência", 
            "N. Sra. Aparecida", "Finados", "Proc. República", "Natal"
        };

        for (int i = 0; i < datasFixas.length; i++) {
            if (datasFixas[i][1] == mes) { 
                LocalDate data = LocalDate.of(ano, mes, datasFixas[i][0]);
                feriados.add(new EventoAgenda(data, "FERIADO", nomesFixos[i], "event-feriado"));
            }
        }
        return feriados;
    }
}