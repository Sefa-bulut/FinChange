package com.example.finchange.operation.service;

import com.example.finchange.operation.dto.HolidayRequest;
import com.example.finchange.operation.dto.HolidayResponse;

import java.time.LocalDate;
import java.util.List;

public interface HolidayService {

    HolidayResponse createHoliday(HolidayRequest request);

    HolidayResponse updateHoliday(Integer id, HolidayRequest request);

    void deleteHoliday(Integer id);

    HolidayResponse getHolidayById(Integer id);

    List<HolidayResponse> getAllHolidays();

    List<HolidayResponse> getHolidaysByDate(LocalDate date);
}
