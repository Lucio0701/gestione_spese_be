package com.gestionespese.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "spring.mail.host")
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Reset Password - Gestione Spese");
        message.setText(
            "Ciao,\n\n" +
            "Hai richiesto il reset della password per il tuo account Gestione Spese.\n\n" +
            "Clicca il link qui sotto per reimpostare la password (valido per 1 ora):\n\n" +
            resetLink + "\n\n" +
            "Se non hai richiesto il reset, ignora questa email.\n\n" +
            "Il team di Gestione Spese"
        );
        mailSender.send(message);
    }
}
