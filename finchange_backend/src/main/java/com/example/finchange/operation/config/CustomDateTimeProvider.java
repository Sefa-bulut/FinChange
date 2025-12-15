package com.example.finchange.operation.config;

import com.example.finchange.operation.service.SystemDateService;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.stereotype.Component;

import java.time.temporal.TemporalAccessor;
import java.util.Optional;

@Component("customDateTimeProvider")
public class CustomDateTimeProvider implements DateTimeProvider {
    private final SystemDateService systemDateService;

    public CustomDateTimeProvider(SystemDateService systemDateService) {
        this.systemDateService = systemDateService;
    }

    @Override
    public Optional<TemporalAccessor> getNow() {
        return Optional.ofNullable(systemDateService.getSystemDateTime());
    }
}
