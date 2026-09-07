package com.ftabah.giftme.application.port;

/** Porta para envio de mensagens de conta, substituível por SMTP ou provedor externo. */
public interface EmailSender {

    void sendVerification(String email, String link);

    void sendPasswordReset(String email, String link);
}