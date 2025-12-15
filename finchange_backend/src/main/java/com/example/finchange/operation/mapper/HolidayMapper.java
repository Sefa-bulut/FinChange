package com.example.finchange.operation.mapper;

import com.example.finchange.operation.dto.HolidayRequest;
import com.example.finchange.operation.dto.HolidayResponse;
import com.example.finchange.operation.model.Holiday;

import java.util.List;
import java.util.stream.Collectors;

public class HolidayMapper {

    // Request -> Entity
    public static Holiday toEntity(HolidayRequest request) {
        Holiday holiday = new Holiday();
        holiday.setHolidayDate(request.getHolidayDate());
        holiday.setType(request.getType());
        holiday.setDescription(request.getDescription());
        return holiday;
    }

    // Entity -> Response
    public static HolidayResponse toResponse(Holiday holiday) {
        return HolidayResponse.builder()
                .id(holiday.getId())
                .holidayDate(holiday.getHolidayDate())
                .type(holiday.getType().name()) // Enum'ı string olarak dönüyoruz
                .description(holiday.getDescription())
                .createdAt(holiday.getCreatedAt())
                .updatedAt(holiday.getUpdatedAt())
                .lastModifiedBy(holiday.getLastModifiedBy())
                .build();
    }

    // Entity List -> Response List
    public static List<HolidayResponse> toResponseList(List<Holiday> holidays) {
        return holidays.stream()
                .map(HolidayMapper::toResponse)
                .collect(Collectors.toList());
    }

    public static void updateEntityFromRequest(Holiday holiday, HolidayRequest request) {
        holiday.setHolidayDate(request.getHolidayDate());
        holiday.setType(request.getType());
        holiday.setDescription(request.getDescription());
    }
}