package com.learningdemo.oauth2clientcredentials.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = TokenController.class)
public class TokenEndpointExceptionHandler {

    @ExceptionHandler(OAuth2TokenException.class)
    public ResponseEntity<OAuth2ErrorResponse> handle(OAuth2TokenException ex) {
        return ResponseEntity.status(ex.status())
                .body(new OAuth2ErrorResponse(ex.errorCode(), ex.getMessage()));
    }
}
