package com.learningdemo.oauth2clientcredentials.web;

import com.fasterxml.jackson.annotation.JsonProperty;

/** RFC 6749 §5.1 access token response. */
public record TokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") long expiresIn,
        @JsonProperty("scope") String scope) {
}
