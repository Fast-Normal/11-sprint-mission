package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponse> handle(DiscodeitException ex) {
    log.warn("DiscodeitException: {}", ex.getMessage());
    return ResponseEntity
        .status(ex.getErrorCode().getStatus())
        .body(ErrorResponse.from(ex));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException ex) {
    Map<String, Object> details = ex.getBindingResult().getFieldErrors()
        .stream()
        .collect(Collectors.toMap(
            FieldError::getField,
            fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "유효하지 않은 값",
            (a, b) -> a
        ));
    log.warn("유효성 검사 실패 - details: {}", details);
    return ResponseEntity.badRequest()
        .body(new ErrorResponse(
            ErrorCode.VALIDATION_ERROR.getStatus().value(),
            ex.getClass().getSimpleName(),
            ErrorCode.VALIDATION_ERROR.getMessage(),
            details,
            Instant.now(),
            ErrorCode.VALIDATION_ERROR.name()
        ));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handle(AccessDeniedException ex) {
    log.warn("적절한 권한 없음", ex);

    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(new ErrorResponse(
            ErrorCode.ACCESS_DENIED.getStatus().value(),
            ex.getClass().getSimpleName(),
            ErrorCode.ACCESS_DENIED.getMessage(),
            Map.of(),
            Instant.now(),
            ErrorCode.ACCESS_DENIED.name()
        ));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnknown(Exception ex) {
    log.error("예상치 못한 예외", ex);
    return ResponseEntity.internalServerError()
        .body(new ErrorResponse(
            ErrorCode.INTERNAL_SERVER_ERROR.getStatus().value(),
            ex.getClass().getSimpleName(),
            ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
            Map.of(),
            Instant.now(),
            ErrorCode.INTERNAL_SERVER_ERROR.name()
        ));
  }
}

