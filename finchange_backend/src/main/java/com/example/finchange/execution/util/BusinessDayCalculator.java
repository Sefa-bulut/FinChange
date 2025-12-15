package com.example.finchange.execution.util;


import com.example.finchange.operation.repository.HolidayRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BusinessDayCalculator{
    private static final Logger log = LoggerFactory.getLogger(BusinessDayCalculator.class);
    private final HolidayRepository holidayRepository;
    private Set<LocalDate> holidayCache = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public BusinessDayCalculator(HolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    @PostConstruct
    public void initializeHolidayCache(){
        log.info("Tatil listesi ilk kez hafızaya yükleniyor...");
        refreshHolidayCache();
    }
    @Scheduled(cron = "0 5 4 * * *")
    public void refreshHolidayCache() {
        this.holidayCache = holidayRepository.findAllHolidayDates();
        log.info("{} adet resmi tatil veritabanından hafızaya yüklendi.", holidayCache.size());
    }
    public LocalDate getBusinessDayBefore(LocalDate fromDate, int businessDaysToSubtract) {
        if (businessDaysToSubtract < 0) {
            throw new IllegalArgumentException("Gün sayısı negatif olamaz");
        }
        if(businessDaysToSubtract == 0){
            return fromDate;
        }
        LocalDate result = fromDate;
        int businessDaysCounted = 0;
        while (businessDaysCounted < businessDaysToSubtract) {
            result = result.minusDays(1);
            if (isBusinessDay(result)) {
                businessDaysCounted++;
            }
        }
        return result;
    }
    public boolean isBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) return false;
        if (holidayCache.contains(date)) return false;
        return true;
    }

    public LocalDate getBusinessDayAfter(LocalDate fromDate, int businessDaysToAdd) {
        if (businessDaysToAdd < 0) {
            throw new IllegalArgumentException("Gün sayısı negatif olamaz");
        }
        LocalDate result = fromDate;
        int businessDaysCounted = 0;
        while (businessDaysCounted < businessDaysToAdd) {
            result = result.plusDays(1);
            if (isBusinessDay(result)) {
                businessDaysCounted++;
            }
        }
        return result;
    }

}
