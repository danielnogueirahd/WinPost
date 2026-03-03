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

    // A mesma pasta definida no Controller
    private final String PASTA_UPLOAD = "uploads/";

    /**
     * Método principal de disparo.
     * Agora recebe o ID do grupo (Long) e apenas os nomes dos arquivos (List<String>),
     * pois os arquivos físicos já foram salvos pelo Controller.
     */
    @Async 
    @Transactional // Garante transação aberta para buscar os contatos do grupo (Evita LazyInitializationException)
    public void enviarDisparo(Long idGrupo, String assunto, String conteudoHtml, List<String> nomesAnexos, LocalDateTime dataAgendamento) {
        
        // 1. Buscamos o grupo pelo ID dentro desta Thread Assíncrona
        Grupo grupo = grupoRepositorio.findById(idGrupo).orElse(null);
        
        if (grupo == null) {
            System.err.println("ERRO CRÍTICO: Grupo não encontrado no disparo assíncrono. ID: " + idGrupo);
            return;
        }

        // 2. Prepara o Log para o Banco de Dados
        MensagemLog log = new MensagemLog();
        log.setAssunto(assunto);
        log.setConteudo(conteudoHtml);
        log.setNomeGrupoDestino(grupo.getNome());
        
        // Cuidado: getContatos() precisa estar dentro do @Transactional
        log.setTotalDestinatarios(grupo.getContatos().size());
        
        // Converte a lista de nomes (List) para uma String única separada por vírgulas para salvar no banco
        if (nomesAnexos != null && !nomesAnexos.isEmpty()) {
            log.setNomesAnexos(String.join(",", nomesAnexos));
        }

        // --- LÓGICA DE AGENDAMENTO ---
        if (dataAgendamento != null) {
            log.setDataEnvio(dataAgendamento);
            log.setStatus("AGENDADO");
            logRepositorio.save(log);
            return; // Se for agendado, paramos aqui. O robô (@Scheduled) pega depois.
        }

        // --- ENVIO IMEDIATO ---
        log.setDataEnvio(LocalDateTime.now());
        log.setStatus("ENVIANDO...");
        log = logRepositorio.save(log);

        // Chama o método que faz o loop e envia os emails
        realizarEnvio(grupo, assunto, conteudoHtml, log);
    }

    /**
     * Robô que roda a cada 60 segundos para verificar se há mensagens agendadas
     */
    @Scheduled(fixedRate = 60000)
    @Transactional // Necessário para carregar os contatos do grupo sem erro
    public void verificarEnviosAgendados() {
        // Busca mensagens com status AGENDADO e data anterior ou igual a agora
        List<MensagemLog> agendados = logRepositorio.findByStatusAndDataEnvioBefore("AGENDADO", LocalDateTime.now());
        
        for (MensagemLog msg : agendados) {
            System.out.println("Processando agendamento ID: " + msg.getId());
            
            // Tenta recuperar o grupo pelo nome salvo no log
            List<Grupo> gruposEncontrados = grupoRepositorio.findByNomeContainingIgnoreCase(msg.getNomeGrupoDestino());
            
            if (!gruposEncontrados.isEmpty()) {
                // Pega o primeiro grupo encontrado (assumindo nomes únicos ou similares)
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

    /**
     * Método privado que efetivamente itera sobre os contatos e envia o email.
     * Busca os anexos diretamente do disco usando o nome salvo no Log.
     */
    private void realizarEnvio(Grupo grupo, String assunto, String conteudoHtml, MensagemLog log) {
        int enviados = 0;
        int erros = 0;

        for (Contatos contato : grupo.getContatos()) {
            try {
                if (contato.getEmail() != null && !contato.getEmail().isEmpty()) {
                    
                    MimeMessage message = mailSender.createMimeMessage();
                    // 'true' indica que é multipart (aceita anexos)
                    MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
                    
                    helper.setFrom("nao-responda@winpost.com");
                    helper.setTo(contato.getEmail());
                    helper.setSubject(assunto);
                    
                    // Substituição de variáveis no texto
                    String corpoFinal = conteudoHtml.replace("{nome}", contato.getNome());
                    helper.setText(corpoFinal, true);

                    // --- ANEXOS ---
                    // Verifica se há nomes de arquivos salvos no log
                    if (log.getNomesAnexos() != null && !log.getNomesAnexos().isEmpty()) {
                        String[] nomesArquivos = log.getNomesAnexos().split(",");
                        
                        for (String nomeFisico : nomesArquivos) {
                            if (!nomeFisico.trim().isEmpty()) {
                                // Busca o arquivo na pasta do sistema
                                File arquivoFisico = new File(PASTA_UPLOAD + nomeFisico.trim());
                                
                                if (arquivoFisico.exists()) {
                                    FileSystemResource fileResource = new FileSystemResource(arquivoFisico);
                                    
                                    // Limpa o nome para o usuário (remove o timestamp 12345_arquivo.pdf -> arquivo.pdf)
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

        // Atualiza o status final no log
        log.setStatus(erros == 0 ? "SUCESSO" : "PARCIAL (" + erros + " erros)");
        logRepositorio.save(log);
    }
    
    // Método para envio simples unitário (ex: recuperação de senha)
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