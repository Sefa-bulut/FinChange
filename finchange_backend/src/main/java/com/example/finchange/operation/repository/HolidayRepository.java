package com.example.finchange.operation.repository;

import com.example.finchange.operation.model.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

public interface HolidayRepository extends JpaRepository<Holiday, Integer> {

    Optional<Holiday> findByHolidayDate(LocalDate date);

    boolean existsByHolidayDate(LocalDate date);

    @Query("SELECT h.holidayDate FROM Holiday h")
    Set<LocalDate> findAllHolidayDates();
}