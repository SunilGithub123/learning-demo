package com.learningdemo.oauth2clientcredentials.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Spring Security's resource-server support validates a JWT's signature and
 * timestamps out of the box, but not its audience - that check is
 * intentionally left to the application, since "who is this token for" is
 * deployment-specific. Rejecting tokens without the expected audience stops
 * a token minted for a different API from being replayed against this one.
 */
final class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final String requiredAudience;

    AudienceValidator(String requiredAudience) {
        this.requiredAudience = requiredAudience;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        if (token.getAudience().contains(requiredAudience)) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(
                new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, "Required audience is missing", null));
    }
}
