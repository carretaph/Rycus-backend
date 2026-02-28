package com.rycus.Rycus_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.internet.MimeMessage;

/**
 * LOCAL ONLY: evita que la app se caiga si no tienes SMTP configurado.
 * En producción (Render) NO se usa este bean.
 */
@Configuration
@Profile("local")
public class LocalMailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        return new JavaMailSender() {

            @Override
            public MimeMessage createMimeMessage() {
                // No se usa en local normalmente; devolver null es OK si tu EmailService no lo llama.
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
                System.out.println("📧 [LOCAL] EmailService send(SimpleMailMessage) TO="
                        + String.join(",", simpleMessage.getTo() == null ? new String[]{} : simpleMessage.getTo())
                        + " SUBJECT=" + simpleMessage.getSubject());
                System.out.println("📧 [LOCAL] BODY=" + simpleMessage.getText());
            }

            @Override
            public void send(SimpleMailMessage... simpleMessages) {
                System.out.println("📧 [LOCAL] EmailService send(SimpleMailMessage...) called - ignored.");
            }
        };
    }
}
