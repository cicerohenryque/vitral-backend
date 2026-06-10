package com.vitral.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.url.base:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.username:noreply@vitral.com}")
    private String remetente;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarConfirmacaoCadastro(String destinatario, String token) {
        if (!mailEnabled || mailSender == null) {
            return;
        }
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject("Vitral — Confirme seu cadastro");
        mensagem.setText("Olá! Clique no link abaixo para confirmar seu cadastro no Vitral:\n\n"
                + baseUrl + "/api/v1/auth/confirmar?token=" + token
                + "\n\nO link expira em 24 horas.\n\nCaso não tenha criado uma conta, ignore este e-mail.");
        mailSender.send(mensagem);
    }
}
