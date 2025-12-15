package com.example.finchange.operation.service;

import com.example.finchange.operation.dto.UpdateSystemTimeRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface SystemDateService {

    LocalDateTime getSystemDateTime();

    LocalDate getSystemDate();

    void updateSystemTime(UpdateSystemTimeRequest request);
}
