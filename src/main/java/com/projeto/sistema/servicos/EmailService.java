package com.projeto.sistema.servicos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.projeto.sistema.modelos.Contatos;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarMensagemEmMassa(String assunto, String corpo, List<Contatos> destinatarios) {
        for (Contatos contato : destinatarios) {
            try {
                if (contato.getEmail() != null && !contato.getEmail().isEmpty()) {
                    SimpleMailMessage message = new SimpleMailMessage();
                    // Coloque aqui o seu e-mail de remetente (tem que ser o mesmo do application.properties se usar Gmail)
                    message.setFrom("nao-responda@winpost.com"); 
                    message.setTo(contato.getEmail());
                    message.setSubject(assunto);
                    
                    // Troca {nome} pelo nome da pessoa
                    String corpoPersonalizado = corpo.replace("{nome}", contato.getNome());
                    message.setText(corpoPersonalizado);

                    mailSender.send(message);
                    System.out.println("E-mail enviado para: " + contato.getEmail());
                }
            } catch (Exception e) {
                System.err.println("Erro ao enviar para " + contato.getEmail() + ": " + e.getMessage());
            }
        }
    }
}