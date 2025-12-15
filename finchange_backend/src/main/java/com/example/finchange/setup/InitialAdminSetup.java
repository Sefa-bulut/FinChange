package com.example.finchange.setup;

import com.example.finchange.user.dto.InviteUserRequestDto;
import com.example.finchange.user.repository.UserRepository;
import com.example.finchange.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitialAdminSetup implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;

    // .env dosyasından ilk admin bilgilerini çekiyoruz
    @Value("${INITIAL_ADMIN_AD}")
    private String adminFirstName;

    @Value("${INITIAL_ADMIN_SOYAD}")
    private String adminLastName;

    @Value("${INITIAL_ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${INITIAL_ADMIN_CODE}")
    private String adminPersonnelCode;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            log.info("Veritabanı boş, ilk yönetici (admin) kullanıcısı oluşturuluyor...");

            InviteUserRequestDto adminDto = new InviteUserRequestDto();
            adminDto.setFirstName(adminFirstName);
            adminDto.setLastName(adminLastName);
            adminDto.setEmail(adminEmail);
            adminDto.setPersonnelCode(adminPersonnelCode);
            adminDto.setRoleNames(List.of("ADMIN"));

            try {
                userService.inviteUser(adminDto);
                log.info("İlk yönetici başarıyla oluşturuldu. E-posta: {}", adminEmail);
            } catch (Exception e) {
                log.error("İlk yönetici oluşturulurken bir hata oluştu.", e);
            }
        } else {
            log.info("Veritabanında zaten kullanıcı mevcut. İlk kurulum adımı atlanıyor.");
        }
    }
}