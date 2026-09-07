package com.ftabah.giftme.application.port;

public interface EmailSender {

    void sendVerification(String email, String link);

    void sendPasswordReset(String email, String link);
}