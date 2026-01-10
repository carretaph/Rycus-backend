package com.rycus.Rycus_backend.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${rycus.mail.from:no-reply@rycus.app}")
    private String mailFrom;

    @Value("${rycus.app.url:https://rycus.app}")
    private String appUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendConnectionInviteEmail(String toEmail, String requesterName) {
        String subject = "New Rycus connection request";
        String link = appUrl + "/connections";

        String text =
                "Hi!\n\n" +
                        requesterName + " sent you a connection request on Rycus.\n\n" +
                        "Open your Network to respond:\n" +
                        link + "\n\n" +
                        "— Rycus";

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(mailFrom);
        msg.setTo(toEmail);
        msg.setSubject(subject);
        msg.setText(text);

        mailSender.send(msg);
    }
}
