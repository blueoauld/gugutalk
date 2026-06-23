package com.blueoauld.server.common.exception

import com.blueoauld.server.common.exception.type.ErrorCode.*
import com.blueoauld.server.common.util.IpExtractor
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = KotlinLogging.logger {}

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(
        e: CustomException,
        servletRequest: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val code = e.errorCode

        print(servletRequest, e.message)
        return ResponseEntity.status(code.status).body(ErrorResponse.of(code))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        e: MethodArgumentNotValidException,
        servletRequest: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val firstError = e.bindingResult.fieldErrors.firstOrNull()
        val message = firstError?.defaultMessage ?: INVALID_INPUT.message

        print(servletRequest, e.message)
        return ResponseEntity.status(INVALID_INPUT.status).body(ErrorResponse.of(INVALID_INPUT, message))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        e: HttpMessageNotReadableException,
        servletRequest: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        print(servletRequest, e.message)
        return ResponseEntity.status(INVALID_INPUT.status).body(ErrorResponse.of(INVALID_INPUT))
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatch(
        e: MethodArgumentTypeMismatchException,
        servletRequest: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        print(servletRequest, e.message)
        return ResponseEntity.status(INVALID_INPUT.status).body(ErrorResponse.of(INVALID_INPUT))
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameter(
        e: MissingServletRequestParameterException,
        servletRequest: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        print(servletRequest, e.message)
        return ResponseEntity.status(INVALID_INPUT.status).body(ErrorResponse.of(INVALID_INPUT))
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(
        e: HttpRequestMethodNotSupportedException,
        servletRequest: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        print(servletRequest, e.message)
        return ResponseEntity.status(METHOD_NOT_ALLOWED.status).body(ErrorResponse.of(METHOD_NOT_ALLOWED))
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFound(
        e: NoResourceFoundException,
        servletRequest: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        print(servletRequest, e.message)
        return ResponseEntity.status(RESOURCE_NOT_FOUND.status).body(ErrorResponse.of(RESOURCE_NOT_FOUND))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        e: IllegalArgumentException,
        servletRequest: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        print(servletRequest, e.message)
        return ResponseEntity.status(INVALID_INPUT.status).body(ErrorResponse.of(INVALID_INPUT))
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(
        e: ResponseStatusException,
        servletRequest: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        print(servletRequest, e.message)
        return ResponseEntity.status(e.statusCode)
            .body(ErrorResponse(code = "HTTP_ERROR", message = e.reason ?: "요청을 처리할 수 없습니다."))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(
        e: Exception,
        servletRequest: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        print(e, servletRequest)
        return ResponseEntity.status(INTERNAL_SERVER_ERROR.status).body(ErrorResponse.of(INTERNAL_SERVER_ERROR))
    }

    private fun print(servletRequest: HttpServletRequest, message: String?) {
        val ip = IpExtractor.extract(servletRequest)

        log.warn {
            "METHOD = ${servletRequest.method}, URI = ${servletRequest.requestURI}, IP = $ip, 메세지 = $message"
        }
    }

    private fun print(e: Exception, servletRequest: HttpServletRequest) {
        val ip = IpExtractor.extract(servletRequest)

        log.error(e) {
            "METHOD = ${servletRequest.method}, URI = ${servletRequest.requestURI}, IP = $ip"
        }
    }
}