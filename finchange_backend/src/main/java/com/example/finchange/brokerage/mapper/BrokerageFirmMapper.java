package com.example.finchange.brokerage.mapper;

import com.example.finchange.brokerage.dto.BrokerageFirmRequest;
import com.example.finchange.brokerage.dto.BrokerageFirmResponse;
import com.example.finchange.brokerage.model.BrokerageFirm;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface BrokerageFirmMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "commissionRate", expression = "java(percentToFraction(req.getCommissionRatePercent()))")
    BrokerageFirm toEntity(BrokerageFirmRequest req);

    @Mapping(target = "commissionRatePercent", expression = "java(fractionToPercent(entity.getCommissionRate()))")
    BrokerageFirmResponse toResponse(BrokerageFirm entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "commissionRate", expression = "java(req.getCommissionRatePercent() != null ? percentToFraction(req.getCommissionRatePercent()) : entity.getCommissionRate())")
    void updateEntity(@MappingTarget BrokerageFirm entity, BrokerageFirmRequest req);

    default BigDecimal percentToFraction(BigDecimal percent) {
        if (percent == null) return null;
        return percent.divide(BigDecimal.valueOf(100));
    }

    default BigDecimal fractionToPercent(BigDecimal fraction) {
        if (fraction == null) return null;
        return fraction.multiply(BigDecimal.valueOf(100));
    }
}
