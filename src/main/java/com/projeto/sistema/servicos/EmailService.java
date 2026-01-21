package com.projeto.sistema.servicos;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.projeto.sistema.modelos.Contatos;
import com.projeto.sistema.modelos.Grupo;
import com.projeto.sistema.modelos.MensagemLog;
import com.projeto.sistema.repositorios.GrupoRepositorio;
import com.projeto.sistema.repositorios.MensagemLogRepositorio;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MensagemLogRepositorio logRepositorio;
    
    @Autowired
    private GrupoRepositorio grupoRepositorio; // Necessário para buscar o grupo no agendamento

    // @Async permite que o sistema libere o usuário enquanto envia em segundo plano
    @Async 
    public void enviarDisparo(Grupo grupo, String assunto, String conteudoHtml, MultipartFile[] anexos, LocalDateTime dataAgendamento) {
        
        // 1. Prepara o Log
        MensagemLog log = new MensagemLog();
        log.setAssunto(assunto);
        log.setConteudo(conteudoHtml);
        log.setNomeGrupoDestino(grupo.getNome());
        log.setTotalDestinatarios(grupo.getContatos().size());
        
        // Salva os nomes dos anexos (apenas para registro, não persistimos o arquivo físico no banco)
        if(anexos != null && anexos.length > 0) {
            String nomes = "";
            for(MultipartFile f : anexos) { 
                if(!f.isEmpty()) nomes += f.getOriginalFilename() + ", "; 
            }
            log.setNomesAnexos(nomes);
        }

        // --- LÓGICA DE AGENDAMENTO ---
        if (dataAgendamento != null) {
            log.setDataEnvio(dataAgendamento);
            log.setStatus("AGENDADO");
            logRepositorio.save(log);
            return; // Interrompe aqui, não envia agora!
        }
        // -----------------------------

        // Se não for agendado, configura para envio imediato
        log.setDataEnvio(LocalDateTime.now());
        log.setStatus("ENVIANDO...");
        log = logRepositorio.save(log);

        // Chama o método que faz o envio real
        realizarEnvio(grupo, assunto, conteudoHtml, anexos, log);
    }

    // Método que verifica o banco a cada 1 minuto (60000ms)
    @Scheduled(fixedRate = 60000)
    public void verificarEnviosAgendados() {
        // Busca mensagens com status AGENDADO e data já passada (ou igual a agora)
        List<MensagemLog> agendados = logRepositorio.findByStatusAndDataEnvioBefore("AGENDADO", LocalDateTime.now());
        
        for (MensagemLog msg : agendados) {
            System.out.println("Processando agendamento ID: " + msg.getId());
            
            // Tenta encontrar o grupo pelo nome salvo no log
            // Nota: O ideal seria salvar o ID do grupo no Log, mas vamos usar o nome conforme sua estrutura atual
            List<Grupo> gruposEncontrados = grupoRepositorio.findByNomeContainingIgnoreCase(msg.getNomeGrupoDestino());
            
            if (!gruposEncontrados.isEmpty()) {
                Grupo grupo = gruposEncontrados.get(0); // Pega o primeiro encontrado
                
                // Atualiza status
                msg.setStatus("ENVIANDO (Agendado)...");
                logRepositorio.save(msg);
                
                // Realiza o envio (Nota: Anexos físicos são perdidos no agendamento nesta versão simples)
                realizarEnvio(grupo, msg.getAssunto(), msg.getConteudo(), null, msg);
                
            } else {
                msg.setStatus("ERRO: Grupo não encontrado");
                logRepositorio.save(msg);
            }
        }
    }

    // Método privado para evitar duplicação de código
    private void realizarEnvio(Grupo grupo, String assunto, String conteudoHtml, MultipartFile[] anexos, MensagemLog log) {
        int enviados = 0;
        int erros = 0;

        for (Contatos contato : grupo.getContatos()) {
            try {
                if (contato.getEmail() != null && !contato.getEmail().isEmpty()) {
                    
                    MimeMessage message = mailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
                    
                    helper.setFrom("nao-responda@winpost.com");
                    helper.setTo(contato.getEmail());
                    helper.setSubject(assunto);
                    
                    String corpoFinal = conteudoHtml.replace("{nome}", contato.getNome());
                    helper.setText(corpoFinal, true);

                    // Anexos (só funcionam no envio imediato nesta versão)
                    if (anexos != null) {
                        for (MultipartFile arquivo : anexos) {
                            if (!arquivo.isEmpty()) {
                                helper.addAttachment(arquivo.getOriginalFilename(), arquivo);
                            }
                        }
                    }

                    mailSender.send(message);
                    enviados++;
                }
            } catch (Exception e) {
                erros++;
                System.err.println("Erro ao enviar para: " + contato.getEmail());
            }
        }

        log.setStatus(erros == 0 ? "SUCESSO" : "PARCIAL (" + erros + " erros)");
        logRepositorio.save(log);
    }
}