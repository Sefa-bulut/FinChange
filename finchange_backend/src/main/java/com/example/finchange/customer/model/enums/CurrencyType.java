package com.example.finchange.customer.model.enums;

public enum CurrencyType {
    TRY("TRY", "Türk Lirası"),
    EUR("EUR", "Euro"),
    USD("USD", "Amerikan Doları");

    private final String code;
    private final String displayName;

    CurrencyType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }
}
