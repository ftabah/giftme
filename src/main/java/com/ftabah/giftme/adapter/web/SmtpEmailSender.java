package com.ftabah.giftme.adapter.web;

import com.ftabah.giftme.application.port.EmailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Sends account action links through the configured SMTP server. */
@Component
@ConditionalOnProperty(prefix = "giftme.email", name = "smtp-enabled", havingValue = "true")
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpEmailSender(JavaMailSender mailSender, @Value("${giftme.email.from:}") String from) {
        if (from.isBlank()) {
            throw new IllegalStateException("GIFTME_EMAIL_FROM must be configured when SMTP is enabled");
        }
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void sendVerification(String email, String link) {
        send(email, "Verifique seu e-mail - GiftMe", "Use este link para verificar seu e-mail: " + link);
    }

    @Override
    public void sendPasswordReset(String email, String link) {
        send(email, "Redefina sua senha - GiftMe", "Use este link para redefinir sua senha: " + link);
    }

    private void send(String email, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}