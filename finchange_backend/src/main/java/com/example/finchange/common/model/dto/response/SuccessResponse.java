package com.example.finchange.common.model.dto.response;

import com.example.finchange.common.util.RandomUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SuccessResponse<T> {

    @Builder.Default
    private final LocalDateTime time = LocalDateTime.now();

    @Builder.Default
    private final String code = RandomUtil.generateUUID();

    @Builder.Default
    private final Boolean isSuccess = true;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;


    public static SuccessResponse<Void> success() {
        return SuccessResponse.<Void>builder()
                .build();
    }

    public static <T> SuccessResponse<T> success(final T content) {
        return SuccessResponse.<T>builder()
                .result(content)
                .build();
    }

    public static <T> SuccessResponse<T> success( String message) {
        return SuccessResponse.<T>builder()
                .message(message)
                .build();
    }

    public static <T> SuccessResponse<T> success(T content, String message) {
        return SuccessResponse.<T>builder()
                .result(content)
                .message(message)
                .build();
    }


}
