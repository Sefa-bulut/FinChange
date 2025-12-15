package com.example.finchange.operation.service.impl;

import com.example.finchange.operation.dto.UpdateSystemTimeRequest;
import com.example.finchange.operation.model.SystemParameters;
import com.example.finchange.operation.repository.SystemParametersRepository;
import com.example.finchange.operation.service.SystemDateService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SystemDateServiceImpl implements SystemDateService {

    private final SystemParametersRepository repository;
    private LocalDateTime cachedSystemTime;

    @PostConstruct
    private void loadInitialSystemTime() {
        this.cachedSystemTime = repository.findById(1)
                .map(SystemParameters::getSystemTime)
                .orElseGet(LocalDateTime::now);
    }

    @Override
    public LocalDateTime getSystemDateTime() {
        return this.cachedSystemTime;
    }

    @Override
    public LocalDate getSystemDate() {
        return this.cachedSystemTime.toLocalDate();
    }

    @Override
    @Transactional
    public void updateSystemTime(UpdateSystemTimeRequest request) {

        SystemParameters params = repository.findById(1)
                .orElseThrow(() -> new IllegalStateException("Sistem parametreleri tablosunda id=1 olan kayıt bulunamadı!"));


        params.setSystemTime(request.getSystemTime());


        if (StringUtils.hasText(request.getDescription())) {
            params.setDescription(request.getDescription());
        }

        repository.save(params);

        this.cachedSystemTime = request.getSystemTime();
    }
}