package dev.rohitahuja.chat.toolcalling;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailTools {

    public void sendEmail(String recipient, String emailBody) {
        // sender
        final String username="rohit23ahuja@gmail.com";
        final String password="";

        // smtp
        String host = "smtp.gmail.com";
        int port = 587;

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);

        // create session
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject("Test Email || LLM");
            message.setText(emailBody);

            Transport.send(message);

            System.out.println("Email sent successfully!");

        } catch (MessagingException e){
            e.printStackTrace();
        }
    }
}
