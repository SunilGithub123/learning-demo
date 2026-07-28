package com.learningdemo.oauth2clientcredentials.web;

import org.springframework.http.HttpStatus;

/** Signals an RFC 6749 §5.2 token-endpoint error (e.g. invalid_client, invalid_scope). */
public class OAuth2TokenException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    public OAuth2TokenException(String errorCode, String description, HttpStatus status) {
        super(description);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String errorCode() {
        return errorCode;
    }

    public HttpStatus status() {
        return status;
    }
}
