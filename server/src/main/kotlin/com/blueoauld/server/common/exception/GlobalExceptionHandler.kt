package com.blueoauld.server.common.exception

import com.blueoauld.server.common.exception.type.ErrorCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e: CustomException): ResponseEntity<ErrorResponse> {
        val code = e.errorCode
        return ResponseEntity.status(code.status).body(ErrorResponse.of(code))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val firstError = e.bindingResult.fieldErrors.firstOrNull()
        val message = firstError?.defaultMessage ?: ErrorCode.INVALID_INPUT.message
        return ResponseEntity.status(ErrorCode.INVALID_INPUT.status)
            .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, message))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(ErrorCode.INVALID_INPUT.status)
            .body(ErrorResponse.of(ErrorCode.INVALID_INPUT))
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.status)
            .body(ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED))
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFound(): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(ErrorCode.RESOURCE_NOT_FOUND.status)
            .body(ErrorResponse.of(ErrorCode.RESOURCE_NOT_FOUND))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(ErrorCode.INVALID_INPUT.status)
            .body(ErrorResponse.of(ErrorCode.INVALID_INPUT))
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(e: ResponseStatusException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(e.statusCode)
            .body(ErrorResponse(code = "HTTP_ERROR", message = e.reason ?: "요청을 처리할 수 없습니다."))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.status)
            .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR))
    }
}