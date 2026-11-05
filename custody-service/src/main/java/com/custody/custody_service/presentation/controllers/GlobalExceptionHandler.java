package com.custody.custody_service.presentation.controllers;

import com.custody.custody_service.domain.exceptions.BusinessException;
import com.custody.custody_service.domain.exceptions.IdempotencyKeyConflictException;
import com.custody.custody_service.domain.exceptions.InsufficientBalanceException;
import com.custody.custody_service.domain.exceptions.InvalidQuantityException;
import com.custody.custody_service.domain.exceptions.InvalidScaleException;
import com.custody.custody_service.domain.exceptions.PortfolioAlreadyExistsException;
import com.custody.custody_service.domain.exceptions.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 — recurso não encontrado (portfólio, ativo, ordem)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // 409 — portfólio já existe para o usuário
    @ExceptionHandler(PortfolioAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handlePortfolioExists(PortfolioAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // 422 — quantidade inválida para ordem
    @ExceptionHandler(InvalidQuantityException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidQuantity(InvalidQuantityException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                "error", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // 422 — saldo insuficiente para ordem SELL
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientBalance(InsufficientBalanceException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                "error", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // 409 — conflito de concorrência otimista (@Version)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "Conflito de concorrência: a posição foi modificada por outra operação. Tente novamente.",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // 409 — reuso de Idempotency-Key com parâmetros diferentes
    @ExceptionHandler(IdempotencyKeyConflictException.class)
    public ResponseEntity<Map<String, Object>> handleIdempotencyConflict(IdempotencyKeyConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // 409 — lock pessimista (transferência concorrente)
    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handlePessimisticLock(PessimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "Conflito de concorrência: registro bloqueado por outra operação. Tente novamente.",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // 422 — escala do quantity excede a escala configurada do ativo
    @ExceptionHandler(InvalidScaleException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidScale(InvalidScaleException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                "error", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // 400 — regra de negócio genérica
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // 400 — argumento inválido
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // 400 — validação de campos (@NotBlank, @NotNull, @DecimalMin, etc)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        f -> f.getField(),
                        f -> f.getDefaultMessage(),
                        (a, b) -> a
                ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "Dados inválidos",
                "fields", errors,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // 400 — JSON malformado
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "Corpo da requisição inválido ou malformado",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // 409 — violação de constraint única no banco
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String message = "Violação de integridade de dados";
        if (ex.getMessage() != null && ex.getMessage().contains("ticker")) {
            message = "Ticker já cadastrado";
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", message,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // 500 — fallback com mensagem genérica (nunca expõe ex.getMessage())
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Erro interno do servidor",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
