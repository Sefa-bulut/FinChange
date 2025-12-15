package com.example.finchange.marketSimulation.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalApiResponseDto {


    @JsonProperty("result")
    @SuppressWarnings("unchecked")
    private void unpackNested(Map<String, Object> result) {
        if (result != null) {
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) {
                this.hisseTanimList = (List<Map<String, Object>>) data.get("HisseTanim");
            }
        }
    }

    // Gerçek veri listesi buraya dolacak.
    private List<Map<String, Object>> hisseTanimList;
}