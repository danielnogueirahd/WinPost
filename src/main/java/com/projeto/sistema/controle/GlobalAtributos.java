package com.projeto.sistema.controle;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.projeto.sistema.modelos.Contatos;
import com.projeto.sistema.modelos.LembreteDTO;
import com.projeto.sistema.modelos.MensagemLog;
import com.projeto.sistema.repositorios.ContatosRepositorio;
import com.projeto.sistema.repositorios.MensagemLogRepositorio;

@ControllerAdvice
public class GlobalAtributos {

    @Autowired
    private MensagemLogRepositorio mensagemRepositorio;

    @Autowired
    private ContatosRepositorio contatosRepositorio;

    @ModelAttribute("notificacoesNaoLidas")
    public long carregarContador() {
        return mensagemRepositorio.countByLidaFalse();
    }

    // Carrega a lista das últimas 5 mensagens
    @ModelAttribute("listaNotificacoes")
    public List<MensagemLog> carregarLista() {
        return mensagemRepositorio.findTop5ByLidaFalseOrderByDataEnvioDesc();
    }
    
    @ModelAttribute("listaLembretes")
    public List<LembreteDTO> carregarLembretesFuturos() {
        List<LembreteDTO> lembretes = new ArrayList<>();
        
        // Lógica: Buscar aniversariantes de AMANHÃ
        LocalDate amanha = LocalDate.now().plusDays(1);
        
        // CORREÇÃO: O nome do método agora bate com o que está no seu Repositório
        List<Contatos> aniversariantesAmanha = contatosRepositorio.findByDiaEMesAniversario(
            amanha.getDayOfMonth(), 
            amanha.getMonthValue()
        );

        // Converte Contatos em LembreteDTO para exibir no modal
        for (Contatos c : aniversariantesAmanha) {
            lembretes.add(new LembreteDTO(
                "Aniversário Amanhã",
                "Não esqueça de parabenizar " + c.getNome(),
                "NIVER",
                amanha,
                c.getId()
            ));
        }

        return lembretes;
    }
    
    @ModelAttribute("qtdLembretes")
    public int contarLembretes() {
        return carregarLembretesFuturos().size();
    }
}