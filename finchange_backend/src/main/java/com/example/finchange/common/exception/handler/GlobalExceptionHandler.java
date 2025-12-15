package com.example.finchange.common.exception.handler;

import com.example.finchange.auth.exception.AccountLockedException;
import com.example.finchange.common.exception.AbstractAlreadyExistsException;
import com.example.finchange.common.exception.AbstractInvalidTransactionException;
import com.example.finchange.common.exception.AbstractNotFoundException;
import com.example.finchange.common.model.dto.response.ErrorResponse;
import com.example.finchange.customer.exception.InvalidTransactionAmountException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import com.example.finchange.execution.exception.MarketClosedException;
import com.example.finchange.execution.exception.InsufficientFundsException;
import com.example.finchange.execution.exception.InsufficientAssetException;
import com.example.finchange.execution.exception.LivePriceUnavailableException;
import com.example.finchange.execution.exception.MaxOrderValueExceededException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleJsonParseErrors(final HttpMessageNotReadableException exception) {
        log.error(exception.getMessage(), exception);

        if (exception.getCause() instanceof InvalidFormatException invalidFormatException) {
            return ErrorResponse.subErrors(invalidFormatException)
                    .header(ErrorResponse.Header.VALIDATION_ERROR.getName())
                    .build();
        }
        return ErrorResponse.builder()
                .header(ErrorResponse.Header.VALIDATION_ERROR.getName())
                .build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleValidationErrors(final MethodArgumentTypeMismatchException exception) {

        log.error(exception.getMessage(), exception);

        return ErrorResponse.subErrors(exception)
                .header(ErrorResponse.Header.VALIDATION_ERROR.getName())
                .build();
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleValidationErrors(final MethodArgumentNotValidException exception) {

        log.error(exception.getMessage(), exception);

        return ErrorResponse.subErrors(exception.getBindingResult().getFieldErrors())
                .header(ErrorResponse.Header.VALIDATION_ERROR.getName())
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handlePathVariableErrors(final ConstraintViolationException exception) {
        log.error(exception.getMessage(), exception);

        return ErrorResponse.subErrors(exception.getConstraintViolations())
                .header(ErrorResponse.Header.VALIDATION_ERROR.getName())
                .build();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    ErrorResponse handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        log.error(exception.getMessage(), exception);

        return ErrorResponse.builder()
                .header(ErrorResponse.Header.API_ERROR.getName())
                .build();
    }






    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ErrorResponse handleAccessDeniedError(final AccessDeniedException exception) {
        log.error(exception.getMessage(), exception);

        return ErrorResponse.builder()
                .header(ErrorResponse.Header.AUTH_ERROR.getName())
                .build();
    }


    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErrorResponse handleSQLError(final SQLException exception) {
        log.error(exception.getMessage(), exception);

        return ErrorResponse.builder()
                .header(ErrorResponse.Header.DATABASE_ERROR.getName())
                .build();
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErrorResponse handleDataAccessException(DataAccessException exception) {

        log.error(exception.getMessage(), exception);

        return ErrorResponse.builder()
                .header(ErrorResponse.Header.DATABASE_ERROR.getName())
                .build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEntityNotFoundException(EntityNotFoundException ex) {
        log.warn("Kaynak bulunamadı: {}", ex.getMessage());
        return ErrorResponse.builder()
                .header(ErrorResponse.Header.NOT_FOUND_ERROR.getName())
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIllegalStateException(IllegalStateException ex) {
        log.warn("İş mantığı hatası: {}", ex.getMessage());
        return ErrorResponse.builder()
                .header(ErrorResponse.Header.CONFLICT_ERROR.getName())
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, OptimisticLockException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleOptimisticLocking(Exception ex) {
        log.warn("Optimistic locking conflict: {}", ex.getMessage());
        return ErrorResponse.builder()
                .header(ErrorResponse.Header.CONFLICT_ERROR.getName())
                .message("Kayıt başka bir işlem tarafından güncellendi. Lütfen sayfayı yenileyip tekrar deneyin.")
                .build();
    }


    @ExceptionHandler(MarketClosedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleMarketClosed(MarketClosedException ex) {
        log.warn("Market closed: {}", ex.getMessage());
        return ErrorResponse.builder()
                .header("MARKET_CLOSED")
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(InsufficientFundsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInsufficientFunds(InsufficientFundsException ex) {
        log.warn("Insufficient funds: {}", ex.getMessage());
        return ErrorResponse.builder()
                .header("INSUFFICIENT_FUNDS")
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(InsufficientAssetException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInsufficientAsset(InsufficientAssetException ex) {
        log.warn("Insufficient asset: {}", ex.getMessage());
        return ErrorResponse.builder()
                .header("INSUFFICIENT_ASSET")
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(LivePriceUnavailableException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleLivePriceUnavailable(LivePriceUnavailableException ex) {
        log.warn("Live price unavailable: {}", ex.getMessage());
        return ErrorResponse.builder()
                .header("LIVE_PRICE_UNAVAILABLE")
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(MaxOrderValueExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMaxOrderValueExceeded(MaxOrderValueExceededException ex) {
        log.warn("Max order value exceeded: {}", ex.getMessage());
        return ErrorResponse.builder()
                .header(ErrorResponse.Header.VALIDATION_ERROR.getName())
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ErrorResponse.builder()
                .header(ErrorResponse.Header.VALIDATION_ERROR.getName())
                .message(ex.getMessage())
                .build();
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErrorResponse handleProcessError(final Exception exception) {
        log.error(exception.getMessage(), exception);

        return ErrorResponse.builder()
                .header(ErrorResponse.Header.PROCESS_ERROR.getName())
                .message(exception.getMessage())
                .build();
    }

    @ExceptionHandler(AccountLockedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ErrorResponse handleAccountLocked(AccountLockedException exception) {
        log.warn("Kilitli hesap giriş denemesi: {}", exception.getMessage());
        return ErrorResponse.builder()
                .header(ErrorResponse.Header.AUTH_ERROR.getName())
                .message(exception.getMessage())
                .build();
    }

    @ExceptionHandler(InvalidTransactionAmountException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidTransactionAmountException(InvalidTransactionAmountException exception) {
        log.error("Geçersiz işlem tutarı: {}", exception.getMessage(), exception);
        return ErrorResponse.builder()
                .header(ErrorResponse.Header.VALIDATION_ERROR.getName())
                .message(exception.getMessage())
                .build();
    }


    // NotFound Exception Handler
    @ExceptionHandler(AbstractNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(AbstractNotFoundException exception) {
        log.error(exception.getMessage(), exception);
        return ErrorResponse.builder()
                .header(ErrorResponse.Header.NOT_FOUND_ERROR.getName())
                .message(exception.getMessage())
                .build();
    }

    @ExceptionHandler(AbstractAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleAlreadyExistsException(AbstractAlreadyExistsException exception) {
        log.error(exception.getMessage(), exception);
        return ErrorResponse.builder()
                .header(ErrorResponse.Header.CONFLICT_ERROR.getName())
                .message(exception.getMessage())
                .build();
    }

    @ExceptionHandler(AbstractInvalidTransactionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidTransactionException(AbstractInvalidTransactionException exception) {
        log.error("Geçersiz işlem veya iş kuralı ihlali: {}", exception.getMessage(), exception);
        return ErrorResponse.builder()
                .header(ErrorResponse.Header.VALIDATION_ERROR.getName()) // Veya yeni bir "BUSINESS_RULE_ERROR"
                .message(exception.getMessage())
                .build();
    }

}
