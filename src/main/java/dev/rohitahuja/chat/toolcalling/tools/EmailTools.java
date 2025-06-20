package dev.rohitahuja.chat.toolcalling.tools;

import dev.rohitahuja.chat.toolcalling.EmailConfigProperties;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.Properties;

public class EmailTools {

    private static final Logger _log = LoggerFactory.getLogger(EmailTools.class);
    private final EmailConfigProperties emailConfigProperties;

    public EmailTools(EmailConfigProperties emailConfigProperties) {
        this.emailConfigProperties = emailConfigProperties;
    }


    @Tool(description = "Send an email to a given recipient with message.")
    public void sendEmail(@ToolParam(description = "recipient of email") String recipient, @ToolParam(description = "message or body of email") String message) {
        _log.info("Email Recipient: {}, Message: {}", recipient, message);
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", 587);

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailConfigProperties.username(), emailConfigProperties.password());
            }
        });

        try {
            Message mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(emailConfigProperties.username()));
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            mimeMessage.setSubject("Test Email");
            mimeMessage.setText(message);

            Transport.send(mimeMessage);
            _log.info("Email sent successfully!");
        } catch (MessagingException e){
            _log.error("Error occurred while sending email", e);
        }
    }
}
