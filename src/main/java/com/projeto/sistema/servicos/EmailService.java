package com.projeto.sistema.servicos;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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
    private GrupoRepositorio grupoRepositorio;

    // ALTERAÇÃO IMPORTANTE: Pasta na raiz do projeto para acesso público via WebConfig
    private final String PASTA_UPLOAD = "uploads/";

    @Async 
    public void enviarDisparo(Grupo grupo, String assunto, String conteudoHtml, MultipartFile[] anexos, LocalDateTime dataAgendamento) {
        
        // 1. Prepara o Log
        MensagemLog log = new MensagemLog();
        log.setAssunto(assunto);
        log.setConteudo(conteudoHtml);
        log.setNomeGrupoDestino(grupo.getNome());
        log.setTotalDestinatarios(grupo.getContatos().size());
        
        // 2. SALVAR ARQUIVOS NO DISCO E NO LOG
        StringBuilder nomesArquivosSalvos = new StringBuilder();

        if (anexos != null && anexos.length > 0) {
            try {
                // Cria a pasta na raiz se não existir
                Files.createDirectories(Paths.get(PASTA_UPLOAD));

                for (MultipartFile arquivo : anexos) { 
                    if (!arquivo.isEmpty()) {
                        String nomeOriginal = StringUtils.cleanPath(arquivo.getOriginalFilename());
                        // Gera nome físico único (Timestamp + Nome)
                        String nomeFisico = System.currentTimeMillis() + "_" + nomeOriginal;
                        
                        // Salva no Disco (na pasta uploads/ da raiz)
                        Path caminho = Paths.get(PASTA_UPLOAD + nomeFisico);
                        Files.copy(arquivo.getInputStream(), caminho, StandardCopyOption.REPLACE_EXISTING);

                        // Adiciona na lista para o banco
                        if (nomesArquivosSalvos.length() > 0) {
                            nomesArquivosSalvos.append(",");
                        }
                        nomesArquivosSalvos.append(nomeFisico); 
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Erro ao salvar anexos no disco: " + e.getMessage());
            }
        }
        
        log.setNomesAnexos(nomesArquivosSalvos.toString());

        // --- LÓGICA DE AGENDAMENTO ---
        if (dataAgendamento != null) {
            log.setDataEnvio(dataAgendamento);
            log.setStatus("AGENDADO");
            logRepositorio.save(log);
            return; // Interrompe aqui. O @Scheduled pegará depois.
        }

        // Se envio imediato
        log.setDataEnvio(LocalDateTime.now());
        log.setStatus("ENVIANDO...");
        log = logRepositorio.save(log);

        realizarEnvio(grupo, assunto, conteudoHtml, anexos, log);
    }

    // Robô que verifica agendamentos a cada minuto
    @Scheduled(fixedRate = 60000)
    public void verificarEnviosAgendados() {
        List<MensagemLog> agendados = logRepositorio.findByStatusAndDataEnvioBefore("AGENDADO", LocalDateTime.now());
        
        for (MensagemLog msg : agendados) {
            System.out.println("Processando agendamento ID: " + msg.getId());
            
            List<Grupo> gruposEncontrados = grupoRepositorio.findByNomeContainingIgnoreCase(msg.getNomeGrupoDestino());
            
            if (!gruposEncontrados.isEmpty()) {
                Grupo grupo = gruposEncontrados.get(0);
                
                msg.setStatus("ENVIANDO (Agendado)...");
                logRepositorio.save(msg);
                
                // Passamos null nos anexos, pois o método realizarEnvio buscará do disco
                realizarEnvio(grupo, msg.getAssunto(), msg.getConteudo(), null, msg);
                
            } else {
                msg.setStatus("ERRO: Grupo não encontrado");
                logRepositorio.save(msg);
            }
        }
    }

    private void realizarEnvio(Grupo grupo, String assunto, String conteudoHtml, MultipartFile[] anexosMemoria, MensagemLog log) {
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

                    // --- LÓGICA HÍBRIDA DE ANEXOS ---
                    
                    // CENÁRIO 1: Envio Imediato (Usa os arquivos da memória RAM se disponíveis)
                    if (anexosMemoria != null) {
                        for (MultipartFile arquivo : anexosMemoria) {
                            if (!arquivo.isEmpty()) {
                                helper.addAttachment(arquivo.getOriginalFilename(), arquivo);
                            }
                        }
                    } 
                    // CENÁRIO 2: Envio Agendado OU Imediato (Busca do disco se não vier da memória)
                    // Isso garante que se o envio imediato falhar e tentar de novo, ele acha o arquivo
                    if (log.getNomesAnexos() != null && !log.getNomesAnexos().isEmpty()) {
                        // Só tenta buscar do disco se não já tiver anexado via memória (evita duplicidade)
                        if (anexosMemoria == null || anexosMemoria.length == 0) {
                            String[] nomesArquivos = log.getNomesAnexos().split(",");
                            
                            for (String nomeFisico : nomesArquivos) {
                                File arquivoFisico = new File(PASTA_UPLOAD + nomeFisico.trim());
                                if (arquivoFisico.exists()) {
                                    FileSystemResource fileResource = new FileSystemResource(arquivoFisico);
                                    // Tenta extrair o nome original (remove o timestamp)
                                    String nomeOriginal = nomeFisico.contains("_") ? nomeFisico.substring(nomeFisico.indexOf("_") + 1) : nomeFisico;
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
                System.err.println("Erro ao enviar para: " + contato.getEmail());
            }
        }

        log.setStatus(erros == 0 ? "SUCESSO" : "PARCIAL (" + erros + " erros)");
        logRepositorio.save(log);
    }
}