package com.example.finchange.marketSimulation.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceApiResponseDto {

    // Bu DTO'nun, gelen listedeki veriyi doğrudan OhlcDataDto'ya çevirmesi için
    // biraz daha gelişmiş bir mantık kullanacağız.
    private OhlcDataDto ohlcData;

    @JsonProperty("result")
    @SuppressWarnings("unchecked")
    private void unpackNested(Map<String, Object> result) {
        if (result == null) return;
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        if (data == null) return;
        List<Map<String, Object>> hisseFiyatList = (List<Map<String, Object>>) data.get("HisseFiyat");

        // Eğer liste boş değilse, ilk elemanı alıp OhlcDataDto'ya çevir.
        if (hisseFiyatList != null && !hisseFiyatList.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            // Listenin ilk elemanını alıyoruz, çünkü tek bir hisse sorduk.
            this.ohlcData = mapper.convertValue(hisseFiyatList.get(0), OhlcDataDto.class);
        }
    }
}