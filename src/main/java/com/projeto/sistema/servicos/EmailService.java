package com.projeto.sistema.servicos;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.projeto.sistema.modelos.Contatos;
import com.projeto.sistema.modelos.Grupo;
import com.projeto.sistema.modelos.MensagemLog;
import com.projeto.sistema.repositorios.MensagemLogRepositorio;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MensagemLogRepositorio logRepositorio;

    // @Async permite que o sistema libere o usuário enquanto envia em segundo plano
    @Async 
    public void enviarDisparo(Grupo grupo, String assunto, String conteudoHtml, MultipartFile[] anexos) {
        
        // 1. Prepara o Log
        MensagemLog log = new MensagemLog();
        log.setAssunto(assunto);
        log.setConteudo(conteudoHtml);
        log.setNomeGrupoDestino(grupo.getNome());
        log.setTotalDestinatarios(grupo.getContatos().size());
        log.setDataEnvio(LocalDateTime.now());
        log.setStatus("ENVIANDO..."); // Status inicial
        
        // Salva os nomes dos anexos (se houver)
        if(anexos != null && anexos.length > 0) {
            String nomes = "";
            for(MultipartFile f : anexos) { 
                if(!f.isEmpty()) nomes += f.getOriginalFilename() + ", "; 
            }
            log.setNomesAnexos(nomes);
        }

        // Salva inicialmente no banco
        log = logRepositorio.save(log);

        int enviados = 0;
        int erros = 0;

        // 2. Loop de Envio
        for (Contatos contato : grupo.getContatos()) {
            try {
                if (contato.getEmail() != null && !contato.getEmail().isEmpty()) {
                    
                    MimeMessage message = mailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
                    
                    helper.setFrom("nao-responda@winpost.com"); // Configure seu remetente aqui
                    helper.setTo(contato.getEmail());
                    helper.setSubject(assunto);
                    
                    // Personaliza o corpo ({nome} -> Nome do Contato)
                    String corpoFinal = conteudoHtml.replace("{nome}", contato.getNome());
                    helper.setText(corpoFinal, true); // true = HTML ativado

                    // Anexa arquivos
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

        // 3. Atualiza o Log com o resultado final
        log.setStatus(erros == 0 ? "SUCESSO" : "PARCIAL (" + erros + " erros)");
        logRepositorio.save(log);
    }
}