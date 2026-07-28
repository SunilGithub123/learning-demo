package com.learningdemo.oauth2clientcredentials.web;

import com.fasterxml.jackson.annotation.JsonProperty;

/** RFC 6749 §5.2 error response. */
public record OAuth2ErrorResponse(
        @JsonProperty("error") String error,
        @JsonProperty("error_description") String errorDescription) {
}
