package com.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidUrlException.class)
    public ProblemDetail handleInvalidUrl(
            InvalidUrlException exception) {

        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        exception.getMessage()
                );

        problem.setTitle("Invalid URL");
        return problem;
    }

    @ExceptionHandler(ShortUrlNotFoundException.class)
    public ProblemDetail handleNotFound(
            ShortUrlNotFoundException exception) {

        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND,
                        exception.getMessage()
                );

        problem.setTitle("Short URL Not Found");
        return problem;
    }

    @ExceptionHandler(ExpiredUrlException.class)
    public ProblemDetail handleExpiredUrl(
            ExpiredUrlException exception) {

        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.GONE,
                        exception.getMessage()
                );

        problem.setTitle("Short URL Expired");
        return problem;
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ProblemDetail handleRateLimitExceeded(
            RateLimitExceededException exception) {

        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.TOO_MANY_REQUESTS,
                        exception.getMessage()
                );

        problem.setTitle("Rate Limit Exceeded");
        return problem;
    }

    @ExceptionHandler(ShortCodeGenerationException.class)
    public ProblemDetail handleGenerationFailure(
            ShortCodeGenerationException exception) {

        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        exception.getMessage()
                );

        problem.setTitle("Short Code Generation Failed");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
            MethodArgumentNotValidException exception) {

        String message = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Request validation failed");

        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        message
                );

        problem.setTitle("Validation Failed");
        return problem;
    }
}