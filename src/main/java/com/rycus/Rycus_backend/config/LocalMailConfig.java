package com.rycus.Rycus_backend.config;

import jakarta.mail.internet.MimeMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Dummy mail sender.
 * Permite que el backend arranque aunque no haya SMTP configurado.
 * En local solo imprime el email en consola.
 */
@Configuration
public class LocalMailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        return new JavaMailSender() {

            @Override
            public MimeMessage createMimeMessage() {
                return null;
            }

            @Override
            public MimeMessage createMimeMessage(java.io.InputStream contentStream) {
                return null;
            }

            @Override
            public void send(MimeMessage mimeMessage) {
                System.out.println("📧 [LOCAL] EmailService send(MimeMessage) called - ignored.");
            }

            @Override
            public void send(MimeMessage... mimeMessages) {
                System.out.println("📧 [LOCAL] EmailService send(MimeMessage...) called - ignored.");
            }

            @Override
            public void send(SimpleMailMessage simpleMessage) {
                String[] to = simpleMessage.getTo() == null ? new String[]{} : simpleMessage.getTo();

                System.out.println("📧 [LOCAL] EmailService send(SimpleMailMessage)");
                System.out.println("📧 [LOCAL] TO=" + String.join(",", to));
                System.out.println("📧 [LOCAL] SUBJECT=" + simpleMessage.getSubject());
                System.out.println("📧 [LOCAL] BODY=" + simpleMessage.getText());
            }

            @Override
            public void send(SimpleMailMessage... simpleMessages) {
                System.out.println("📧 [LOCAL] EmailService send(SimpleMailMessage...) called - ignored.");
            }
        };
    }
}