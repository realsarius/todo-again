package yapilacaklarListesi.email;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.Authenticator;
import javax.activation.*;
import javax.mail.Session;
import javax.mail.Transport;

public class SendEmail {

    private String email;
    private String emailBasligi;
    private String emailIcerik;
    private String emailSifre;

    public SendEmail(String emailBasligi, String emailIcerik, String email, String emailSifre) {
        this.email = email;
        this.emailBasligi = emailBasligi;
        this.emailIcerik = emailIcerik;
        this.emailSifre = emailSifre;
    }

    public void sendAnEmail() {
        System.out.println("Preparing to send email");
        String to = "lynvx56365@gmail.com";
        String host = "smtp.gmail.com";
        int port = 587;

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.mailtrap.io");
        properties.put("mail.smtp.port", "25");
        properties.put("mail.smtp.ssl.trust", "smtp.mailtrap.io");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, emailSifre);
            }
        });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(email));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(emailBasligi);
            message.setText(emailIcerik);
            Transport.send(message);
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }

    }


}