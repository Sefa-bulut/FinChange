package com.example.finchange.report.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class KpiDTO {
    Double periodReturnPct;
    Double benchmarkPct;
    String benchmarkCode;
    String riskGroup;
}
