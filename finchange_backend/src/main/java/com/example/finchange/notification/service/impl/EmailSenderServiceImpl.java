package com.example.finchange.notification.service.impl;

import com.example.finchange.notification.service.EmailSenderService;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class EmailSenderServiceImpl implements EmailSenderService {

    private final SendGrid sendGrid;

    public EmailSenderServiceImpl(@Value("${sendgrid.api-key}") String apiKey) {
        this.sendGrid = new SendGrid(apiKey);
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String firstName, String tempPassword) {
        Email from = new Email("finchange.service@gmail.com", "FinChange");
        Email to = new Email(toEmail);
        String subject = "FinChange Platformuna Hoş Geldiniz!";
        String loginUrl = "http://localhost:3000/login";

        String htmlContent = String.format("""
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px;">
                <h2 style="color: #c8102e;">Merhaba %s, FinChange'e Hoş Geldiniz!</h2>
                <p>FinChange platformuna erişiminiz oluşturulmuştur. Aşağıdaki bilgilerle giriş yapabilirsiniz:</p>
                <p><strong>Kullanıcı Adı (E-posta):</strong> %s</p>
                <p><strong>Geçici Şifreniz:</strong> <code style="background-color:#f0f0f0; padding: 3px 5px; border-radius:3px;">%s</code></p>
                <p>Giriş yapmak için aşağıdaki butona tıklayabilirsiniz. İlk girişinizde bu şifreyi değiştirmeniz istenecektir.</p>
                <p><a href="%s" style="background-color: #c8102e; color: white; padding: 10px 15px; text-decoration: none; border-radius: 4px;">Platforma Giriş Yap</a></p>
                <hr>
                <small style="color: #777;">Bu e-posta, FinChange sistemi tarafından otomatik olarak gönderilmiştir.</small>
            </div>
            """, firstName, toEmail, tempPassword, loginUrl);

        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        sendEmailRequest(toEmail, "Davet", mail);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {
        Email from = new Email("finchange.service@gmail.com", "FinChange");
        Email to = new Email(toEmail);
        String subject = "FinChange Şifre Sıfırlama Talebi";

        String htmlContent = String.format("""
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px;">
            <h2 style="color: #c8102e;">Şifre Sıfırlama Kodu</h2>
            <p>Merhaba,</p>
            <p>Hesabınız için bir şifre sıfırlama talebi aldık. Eğer bu talebi siz yapmadıysanız, bu e-postayı görmezden gelebilirsiniz.</p>
            <p>Şifrenizi sıfırlamak için kullanmanız gereken 6 haneli doğrulama kodunuz aşağıdadır:</p>
            <p style="text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 5px; background-color: #f0f0f0; padding: 15px; border-radius: 5px;">
                %s
            </p>
            <p>Bu kod <strong>5 dakika</strong> boyunca geçerlidir.</p>
            <hr>
            <small style="color: #777;">Bu e-posta, FinChange sistemi tarafından otomatik olarak gönderilmiştir.</small>
        </div>
        """, token);

        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        sendEmailRequest(toEmail, "Şifre sıfırlama", mail);
    }

    private void sendEmailRequest(String toEmail, String logType, Mail mail) {
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("{} e-postası başarıyla gönderildi: {}. Status Kodu: {}", logType, toEmail, response.getStatusCode());
            } else {
                log.error("SendGrid {} e-postasını gönderemedi: {}. Status Kodu: {}, Body: {}", logType, toEmail, response.getStatusCode(), response.getBody());
                throw new RuntimeException("SendGrid e-posta gönderemedi. Status: " + response.getStatusCode());
            }
        } catch (IOException e) {
            log.error("SendGrid API'sine bağlanırken hata oluştu.", e);
            throw new RuntimeException("SendGrid API ile e-posta gönderilirken hata oluştu", e);
        }
    }
}