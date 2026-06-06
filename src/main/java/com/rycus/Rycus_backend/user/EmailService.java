package com.rycus.Rycus_backend.user;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${rycus.mail.from:no-reply@rycus.app}")
    private String mailFrom;

    @Value("${rycus.app.url:https://rycus.app}")
    private String appUrl;

    private static final String WELCOME_IMAGE_URL =
            "https://rycus.app/rycus-welcome.png";

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

    public void sendWelcomeEmail(String toEmail, String fullName) {

        System.out.println("=================================");
        System.out.println("SENDING WELCOME EMAIL");
        System.out.println("TO: " + toEmail);
        System.out.println("FROM: " + mailFrom);
        System.out.println("=================================");

        try {
            String safeName = (fullName != null && !fullName.isBlank())
                    ? fullName.trim()
                    : "there";

            String subject = "Welcome to Rycus 🎉";

            String html =
                    "<div style='font-family:Arial,sans-serif;background:#f4f7fb;padding:24px;'>" +
                            "  <div style='max-width:680px;margin:0 auto;background:#ffffff;border-radius:18px;overflow:hidden;border:1px solid #e5e7eb;'>" +
                            "    <img src='" + WELCOME_IMAGE_URL + "' alt='Welcome to Rycus' style='width:100%;display:block;' />" +
                            "    <div style='padding:28px;'>" +
                            "      <h1 style='color:#0b1f4d;margin:0 0 12px;font-size:28px;'>Welcome, " + safeName + "!</h1>" +
                            "      <p style='color:#334155;font-size:16px;line-height:1.6;margin:0 0 16px;'>" +
                            "        Congratulations and welcome to Rycus. You are now part of a trusted professional referral network built for contractors, sales professionals and local service providers." +
                            "      </p>" +
                            "      <p style='color:#334155;font-size:16px;line-height:1.6;margin:0 0 22px;'>" +
                            "        Start connecting, sharing verified reviews, and growing real opportunities with trusted professionals." +
                            "      </p>" +
                            "      <a href='" + appUrl + "' style='display:inline-block;background:#1264ff;color:#ffffff;text-decoration:none;padding:14px 22px;border-radius:12px;font-weight:700;'>" +
                            "        Open Rycus" +
                            "      </a>" +
                            "      <p style='color:#64748b;font-size:13px;line-height:1.5;margin-top:24px;'>" +
                            "        If the button does not work, open: " + appUrl +
                            "      </p>" +
                            "    </div>" +
                            "  </div>" +
                            "</div>";

            System.out.println("Creating MimeMessage...");

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);

            System.out.println("Sending email through Gmail SMTP...");

            mailSender.send(message);

            System.out.println("=================================");
            System.out.println("WELCOME EMAIL SENT SUCCESSFULLY");
            System.out.println("TO: " + toEmail);
            System.out.println("=================================");

        } catch (Exception ex) {
            System.out.println("=================================");
            System.out.println("WELCOME EMAIL FAILED");
            System.out.println("TO: " + toEmail);
            System.out.println("ERROR: " + ex.getMessage());
            System.out.println("=================================");

            ex.printStackTrace();
        }
    }
}