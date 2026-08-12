package com.example.ms_auth.exceptions

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import com.example.ms_auth.dtos.ErrorDTO

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(
        exception: BadCredentialsException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorDTO> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorDTO(
                status = HttpStatus.BAD_REQUEST.value(),
                error = HttpStatus.BAD_REQUEST.name,
                message = exception.message,
                path = request.servletPath
            )
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        exception: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorDTO> {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorDTO(
                status = HttpStatus.BAD_REQUEST.value(),
                error = HttpStatus.BAD_REQUEST.name,
                message = "Malformed JSON or missing required fields",
                path = request.servletPath
            )
        )
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(
        exception: BadRequestException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorDTO> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorDTO(
                status = HttpStatus.BAD_REQUEST.value(),
                error = HttpStatus.BAD_REQUEST.name,
                message = exception.message,
                path = request.servletPath
            )
        )
    }

    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidTokenException(
        exception: InvalidTokenException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorDTO> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ErrorDTO(
                status = HttpStatus.UNAUTHORIZED.value(),
                error = HttpStatus.UNAUTHORIZED.name,
                message = exception.message,
                path = request.servletPath
            )
        )
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(
        exception: NotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorDTO> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorDTO(
                status = HttpStatus.NOT_FOUND.value(),
                error = HttpStatus.NOT_FOUND.name,
                message = exception.message,
                path = request.servletPath
            )
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationError(
        exception: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ErrorDTO {
        val errorMessage = HashMap<String, String?>()
        exception.bindingResult.fieldErrors.forEach { e ->
            errorMessage[e.field] = e.defaultMessage
        }
        return ErrorDTO(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.name,
            message = errorMessage.toString(),
            path = request.servletPath
        )
    }
}