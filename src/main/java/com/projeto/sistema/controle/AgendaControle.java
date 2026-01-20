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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.projeto.sistema.modelos.Contatos;
import com.projeto.sistema.modelos.EventoAgenda;
import com.projeto.sistema.modelos.MensagemLog;
import com.projeto.sistema.repositorios.ContatosRepositorio;
import com.projeto.sistema.repositorios.MensagemLogRepositorio;

@Controller
public class AgendaControle {

    @Autowired
    private ContatosRepositorio contatosRepositorio;

    @Autowired
    private MensagemLogRepositorio mensagemRepositorio;

    @GetMapping("/agenda")
    public ModelAndView acessarAgenda(@RequestParam(required = false) Integer mes, 
                                      @RequestParam(required = false) Integer ano) {
        
        ModelAndView mv = new ModelAndView("administrativo/agenda");
        
        // 1. Lógica de Datas
        LocalDate hoje = LocalDate.now();
        int mesAtual = (mes != null) ? mes : hoje.getMonthValue();
        int anoAtual = (ano != null) ? ano : hoje.getYear();
        
        YearMonth anoMes = YearMonth.of(anoAtual, mesAtual);
        LocalDateTime inicioMes = anoMes.atDay(1).atStartOfDay();
        LocalDateTime fimMes = anoMes.atEndOfMonth().atTime(LocalTime.MAX);

        // 2. Busca Dados do Banco
        List<Contatos> aniversariantes = contatosRepositorio.findByMesAniversario(mesAtual);
        List<MensagemLog> envios = mensagemRepositorio.findByDataEnvioBetween(inicioMes, fimMes);

        // 3. Lista Unificada de Eventos
        List<EventoAgenda> eventos = new ArrayList<>();

        // -> Adiciona Feriados (NOVO)
        eventos.addAll(getFeriadosDoMes(mesAtual, anoAtual));

        // -> Adiciona Aniversariantes
        for (Contatos c : aniversariantes) {
            LocalDate dataNiver = LocalDate.of(anoAtual, mesAtual, c.getDataNascimento().getDayOfMonth());
            eventos.add(new EventoAgenda(dataNiver, "NIVER", c.getNome(), "event-niver"));
        }

        // -> Adiciona Envios
        for (MensagemLog log : envios) {
            eventos.add(new EventoAgenda(log.getDataEnvio().toLocalDate(), "ENVIO", log.getAssunto(), "event-envio"));
        }

        // 4. Ordena
        eventos.sort(Comparator.comparing(EventoAgenda::getData));

        // 5. Envia para a Tela
        mv.addObject("listaEventos", eventos);
        mv.addObject("paginaAtiva", "agenda");
        mv.addObject("mesExibicao", mesAtual);
        mv.addObject("anoExibicao", anoAtual);
        mv.addObject("totalDiasMes", anoMes.lengthOfMonth());
        
        int diaSemanaPrimeiroDia = anoMes.atDay(1).getDayOfWeek().getValue();
        mv.addObject("diaSemanaInicio", diaSemanaPrimeiroDia); 
        
        return mv;
    }

    // --- MÉTODO AUXILIAR: Lista de Feriados Fixos ---
    private List<EventoAgenda> getFeriadosDoMes(int mes, int ano) {
        List<EventoAgenda> feriados = new ArrayList<>();
        
        // Formato: Dia, Mês, Nome
        int[][] datasFixas = {
            {1, 1},   // Confraternização Universal
            {21, 4},  // Tiradentes
            {1, 5},   // Dia do Trabalho
            {7, 9},   // Independência do Brasil
            {12, 10}, // Nossa Sra. Aparecida
            {2, 11},  // Finados
            {15, 11}, // Proclamação da República
            {25, 12}  // Natal
        };
        
        String[] nomesFixos = {
            "Ano Novo", "Tiradentes", "Dia do Trabalho", "Independência", 
            "N. Sra. Aparecida", "Finados", "Proc. República", "Natal"
        };

        for (int i = 0; i < datasFixas.length; i++) {
            if (datasFixas[i][1] == mes) { // Se o feriado for neste mês
                LocalDate data = LocalDate.of(ano, mes, datasFixas[i][0]);
                feriados.add(new EventoAgenda(data, "FERIADO", nomesFixos[i], "event-feriado"));
            }
        }
        
        return feriados;
    }
 // --- NOVO: API para buscar detalhes do dia via AJAX ---
    @GetMapping("/agenda/detalhes")
    @org.springframework.web.bind.annotation.ResponseBody // Indica que retorna JSON, não HTML
    public List<com.projeto.sistema.modelos.DetalheAgendaDTO> obterDetalhesDia(
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        
        List<com.projeto.sistema.modelos.DetalheAgendaDTO> detalhes = new ArrayList<>();

        // 1. Busca Aniversariantes do dia
        List<Contatos> nivers = contatosRepositorio.findByDiaEMesAniversario(data.getDayOfMonth(), data.getMonthValue());
        for (Contatos c : nivers) {
            detalhes.add(new com.projeto.sistema.modelos.DetalheAgendaDTO(
                "NIVER", 
                c.getNome(), 
                c.getEmail(), // Subtítulo é o e-mail
                c.getId()
            ));
        }

        // 2. Busca Envios do dia (00:00 até 23:59)
        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.atTime(LocalTime.MAX);
        List<MensagemLog> msgs = mensagemRepositorio.findByDataEnvioBetween(inicio, fim);
        
        for (MensagemLog m : msgs) {
            detalhes.add(new com.projeto.sistema.modelos.DetalheAgendaDTO(
                "ENVIO", 
                m.getAssunto(), 
                "Grupo: " + m.getNomeGrupoDestino(), // Subtítulo é o grupo
                m.getId()
            ));
        }
        
        // 3. Feriados (Lógica simples)
        List<EventoAgenda> feriados = getFeriadosDoMes(data.getMonthValue(), data.getYear());
        for (EventoAgenda f : feriados) {
            if (f.getData().isEqual(data)) {
                detalhes.add(new com.projeto.sistema.modelos.DetalheAgendaDTO("FERIADO", f.getTitulo(), "Feriado Nacional", null));
            }
        }

        return detalhes;
    }
}