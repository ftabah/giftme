package com.ftabah.giftme.adapter.web;

import com.ftabah.giftme.application.port.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Sender local que registra links; substitua por SMTP em produção. */
@Component
@ConditionalOnProperty(prefix = "giftme.email", name = "smtp-enabled", havingValue = "false", matchIfMissing = true)
public class ConsoleEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(ConsoleEmailSender.class);

    @Override
    public void sendVerification(String email, String link) {
        log.info("Email verification link for {}: {}", email, link);
    }

    @Override
    public void sendPasswordReset(String email, String link) {
        log.info("Password reset link for {}: {}", email, link);
    }
}