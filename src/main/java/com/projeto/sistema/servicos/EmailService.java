package com.projeto.sistema.servicos;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private GrupoRepositorio grupoRepositorio;

    private final String PASTA_UPLOAD = "uploads/";

    @Async 
    @Transactional 
    public void enviarDisparo(Long idGrupo, String assunto, String conteudoHtml, List<String> nomesAnexos, LocalDateTime dataAgendamento) {
        
        Grupo grupo = grupoRepositorio.findById(idGrupo).orElse(null);
        
        if (grupo == null) {
            System.err.println("ERRO CRÍTICO: Grupo não encontrado no disparo assíncrono. ID: " + idGrupo);
            return;
        }

        MensagemLog log = new MensagemLog();
        log.setAssunto(assunto);
        log.setConteudo(conteudoHtml);
        log.setNomeGrupoDestino(grupo.getNome());
        
        // <-- A MÁGICA: O Log herda a empresa do Grupo automaticamente!
        log.setEmpresa(grupo.getEmpresa()); 
        
        log.setTotalDestinatarios(grupo.getContatos().size());
        
        if (nomesAnexos != null && !nomesAnexos.isEmpty()) {
            log.setNomesAnexos(String.join(",", nomesAnexos));
        }

        if (dataAgendamento != null) {
            log.setDataEnvio(dataAgendamento);
            log.setStatus("AGENDADO");
            logRepositorio.save(log);
            return; 
        }

        log.setDataEnvio(LocalDateTime.now());
        log.setStatus("ENVIANDO...");
        log = logRepositorio.save(log);

        realizarEnvio(grupo, assunto, conteudoHtml, log);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional 
    public void verificarEnviosAgendados() {
        List<MensagemLog> agendados = logRepositorio.findByStatusAndDataEnvioBefore("AGENDADO", LocalDateTime.now());
        
        for (MensagemLog msg : agendados) {
            System.out.println("Processando agendamento ID: " + msg.getId());
            
            // <-- BLINDAGEM: O Robô agora busca o grupo pelo nome E PELA EMPRESA da mensagem!
            List<Grupo> gruposEncontrados = grupoRepositorio.findByNomeContainingIgnoreCaseAndEmpresa(msg.getNomeGrupoDestino(), msg.getEmpresa());
            
            if (!gruposEncontrados.isEmpty()) {
                Grupo grupo = gruposEncontrados.get(0);
                
                msg.setStatus("ENVIANDO (Agendado)...");
                logRepositorio.save(msg);
                
                realizarEnvio(grupo, msg.getAssunto(), msg.getConteudo(), msg);
                
            } else {
                msg.setStatus("ERRO: Grupo não encontrado");
                logRepositorio.save(msg);
            }
        }
    }

    private void realizarEnvio(Grupo grupo, String assunto, String conteudoHtml, MensagemLog log) {
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

                    if (log.getNomesAnexos() != null && !log.getNomesAnexos().isEmpty()) {
                        String[] nomesArquivos = log.getNomesAnexos().split(",");
                        
                        for (String nomeFisico : nomesArquivos) {
                            if (!nomeFisico.trim().isEmpty()) {
                                File arquivoFisico = new File(PASTA_UPLOAD + nomeFisico.trim());
                                
                                if (arquivoFisico.exists()) {
                                    FileSystemResource fileResource = new FileSystemResource(arquivoFisico);
                                    
                                    String nomeOriginal = nomeFisico.contains("_") ? 
                                                          nomeFisico.substring(nomeFisico.indexOf("_") + 1) : 
                                                          nomeFisico;
                                                          
                                    helper.addAttachment(nomeOriginal, fileResource);
                                }
                            }
                        }
                    }

                    mailSender.send(message);
                    enviados++;
                }
            } catch (Exception e) {
                erros++;
                System.err.println("Erro ao enviar email para: " + contato.getEmail() + " - " + e.getMessage());
            }
        }

        log.setStatus(erros == 0 ? "SUCESSO" : "PARCIAL (" + erros + " erros)");
        logRepositorio.save(log);
    }
    
    @Async
    public void enviarEmailSimples(String para, String assunto, String conteudo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            
            helper.setFrom("nao-responda@winpost.com");
            helper.setTo(para);
            helper.setSubject(assunto);
            helper.setText(conteudo, true);
            
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro no envio individual: " + e.getMessage());
        }
    }
}