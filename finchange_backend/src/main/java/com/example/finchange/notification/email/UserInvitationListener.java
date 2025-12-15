package com.example.finchange.notification.email;

import com.example.finchange.notification.service.EmailSenderService;
import com.example.finchange.user.dto.UserInvitationEvent;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;


@Component
public class UserInvitationListener {
    private static final Logger log = LoggerFactory.getLogger(UserInvitationListener.class);
    private final EmailSenderService emailSenderService;

    public UserInvitationListener(EmailSenderService emailSenderService) {
        this.emailSenderService = emailSenderService;
    }

    @KafkaListener(topics = "user-invitation-events", groupId = "finChange-email-group")
    public void handleUserInvitation(UserInvitationEvent event) {
        log.info("Yeni davet alındı: {}", event.getEmail());

        try {
            emailSenderService.sendWelcomeEmail(
                    event.getEmail(),
                    event.getFirstName(),
                    event.getTemporaryPassword()
            );
            log.info("Davet e-postası gönderildi. {}", event.getEmail());
        } catch (Exception e) {
            log.error("Davet e-postası gönderilemedi. {}", event.getEmail(), e);
            throw e;
        }
    }
}