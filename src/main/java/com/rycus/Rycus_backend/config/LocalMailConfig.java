package com.rycus.Rycus_backend.config;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

/**
 * Dummy mail sender ONLY for local development.
 * In production, Gmail SMTP from Spring Boot will be used.
 */
@Configuration
@Profile("local")
public class LocalMailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        return new JavaMailSender() {

            @Override
            public MimeMessage createMimeMessage() {
                return new MimeMessage(
                        Session.getInstance(new Properties())
                );
            }

            @Override
            public MimeMessage createMimeMessage(java.io.InputStream contentStream) {
                try {
                    return new MimeMessage(
                            Session.getInstance(new Properties()),
                            contentStream
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
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