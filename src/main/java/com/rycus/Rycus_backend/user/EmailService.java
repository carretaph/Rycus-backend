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
            "https://res.cloudinary.com/dywqugq2q/image/upload/WELCOME_TO_RYCUS_qmacqm.png";

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

        try {
            String safeName =
                    (fullName != null && !fullName.isBlank())
                            ? fullName.trim()
                            : "there";

            String subject = "Welcome to Rycus 🎉";

            String html =
                    "<div style='font-family:Arial,sans-serif;background:#f4f7fb;padding:24px;margin:0;'>" +
                            "  <div style='max-width:820px;margin:0 auto;background:#ffffff;border-radius:18px;overflow:hidden;border:1px solid #e5e7eb;box-shadow:0 8px 24px rgba(15,23,42,0.08);'>" +

                            "    <img src='" + WELCOME_IMAGE_URL + "' " +
                            "         alt='Welcome to Rycus' " +
                            "         style='width:100%;max-width:820px;height:auto;display:block;border:0;outline:none;text-decoration:none;' />" +

                            "    <div style='padding:26px 28px 30px;text-align:center;'>" +

                            "      <h1 style='color:#0b1f4d;margin:0 0 12px;font-size:28px;line-height:1.25;font-weight:800;'>" +
                            "        Welcome, " + safeName + "!" +
                            "      </h1>" +

                            "      <p style='color:#334155;font-size:16px;line-height:1.6;margin:0 auto 18px;max-width:640px;'>" +
                            "        Your Rycus account has been created successfully. You are now part of a trusted professional network built to help contractors, sales professionals and local service providers connect, refer and grow." +
                            "      </p>" +

                            "      <a href='" + appUrl + "' " +
                            "         style='display:inline-block;background:#1264ff;color:#ffffff;text-decoration:none;padding:14px 26px;border-radius:12px;font-weight:700;font-size:15px;'>" +
                            "        Open Rycus" +
                            "      </a>" +

                            "      <p style='color:#64748b;font-size:13px;line-height:1.5;margin:22px 0 0;'>" +
                            "        If the button does not work, open: " + appUrl +
                            "      </p>" +

                            "    </div>" +
                            "  </div>" +
                            "</div>";

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);

        } catch (Exception ex) {
            System.out.println("WELCOME EMAIL FAILED FOR: " + toEmail);
            ex.printStackTrace();
        }
    }
}