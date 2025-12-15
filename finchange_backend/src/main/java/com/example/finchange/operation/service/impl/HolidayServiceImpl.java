package com.example.finchange.operation.service.impl;

import com.example.finchange.operation.dto.HolidayRequest;
import com.example.finchange.operation.dto.HolidayResponse;
import com.example.finchange.operation.exception.HolidayAlreadyException;
import com.example.finchange.operation.exception.HolidayNotFoundException;
import com.example.finchange.operation.exception.InvalidHolidayDateException;
import com.example.finchange.operation.mapper.HolidayMapper;
import com.example.finchange.operation.model.Holiday;
import com.example.finchange.operation.repository.HolidayRepository;
import com.example.finchange.operation.service.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class HolidayServiceImpl implements HolidayService {

    private final HolidayRepository holidayRepository;
    private final SystemDateServiceImpl systemDateService;

    @Override
    public HolidayResponse createHoliday(HolidayRequest request) {
        // 2. Merkezi servisimizden o anki sistem tarihini alıyoruz.
        LocalDate currentSystemDate = systemDateService.getSystemDate();
        LocalDate newHolidayDate = request.getHolidayDate();

        // 3. İŞ KURALI: Eklenmek istenen tatil tarihi, sistem tarihinden sonra mı?
        if (!newHolidayDate.isAfter(currentSystemDate)) {
            throw new InvalidHolidayDateException(
                    "Geçersiz tatil tarihi. Tatil, sistem tarihinden (" + currentSystemDate + ") sonraki bir gün olmalıdır."
            );
        }

        // 4. Tarih çakışmasını kontrol et
        if (holidayRepository.existsByHolidayDate(newHolidayDate)) {
            throw new HolidayAlreadyException(newHolidayDate);
        }

        // Eğer tüm kontrollerden geçerse, kaydı oluştur.
        Holiday holiday = HolidayMapper.toEntity(request);
        Holiday saved = holidayRepository.save(holiday);
        return HolidayMapper.toResponse(saved);
    }

    @Override
    public HolidayResponse updateHoliday(Integer id, HolidayRequest request) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new HolidayNotFoundException("Bu tarihle ilgili id bulunamadı: " + id));

        // 1. Merkezi servisimizden o anki sistem tarihini alıyoruz.
        LocalDate currentSystemDate = systemDateService.getSystemDate();
        LocalDate newHolidayDate = request.getHolidayDate();

        // 2. İŞ KURALI: Güncellenen tatil tarihi, sistem tarihinden sonra mı?
        if (!newHolidayDate.isAfter(currentSystemDate)) {
            throw new InvalidHolidayDateException(
                    "Geçersiz tatil tarihi. Tatil, sistem tarihinden (" + currentSystemDate + ") sonraki bir gün olmalıdır."
            );
        }

        // Eğer kontrol başarılıysa, entity'i güncelle ve kaydet.
        HolidayMapper.updateEntityFromRequest(holiday, request);
        Holiday updated = holidayRepository.save(holiday);
        return HolidayMapper.toResponse(updated);
    }



    @Override
    public void deleteHoliday(Integer id) {
        if (!holidayRepository.existsById(id)) {
            throw new HolidayNotFoundException(" bu tarihle ilgili id bulunamadı: " + id);
        }
        holidayRepository.deleteById(id);
    }

    @Override
    public HolidayResponse getHolidayById(Integer id) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new HolidayNotFoundException(" bu tarihle ilgili id bulunamadı: " + id));
        return HolidayMapper.toResponse(holiday);
    }

    @Override
    public List<HolidayResponse> getAllHolidays() {
        List<Holiday> holidays = holidayRepository.findAll();
        return HolidayMapper.toResponseList(holidays);
    }

    @Override
    public List<HolidayResponse> getHolidaysByDate(LocalDate date) {
        return holidayRepository.findByHolidayDate(date)
                .map(holiday -> List.of(HolidayMapper.toResponse(holiday)))
                .orElseGet(List::of);
    }

}