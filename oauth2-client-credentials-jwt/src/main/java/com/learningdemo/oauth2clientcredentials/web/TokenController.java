package com.learningdemo.oauth2clientcredentials.web;

import com.learningdemo.oauth2clientcredentials.config.DemoClientsProperties;
import com.learningdemo.oauth2clientcredentials.config.RegisteredClient;
import com.learningdemo.oauth2clientcredentials.config.RegisteredClientStore;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal RFC 6749 client_credentials token endpoint. A real authorization
 * server (e.g. Spring Authorization Server, Keycloak, Okta) would replace
 * this entirely; it exists here so the whole grant can be exercised - client
 * authentication, scope negotiation, and JWT issuance - inside one demo.
 */
@RestController
public class TokenController {

    private final RegisteredClientStore clientStore;
    private final JwtEncoder jwtEncoder;
    private final long tokenTtlSeconds;
    private final String issuer;
    private final String audience;

    public TokenController(
            RegisteredClientStore clientStore,
            JwtEncoder jwtEncoder,
            DemoClientsProperties properties) {
        this.clientStore = clientStore;
        this.jwtEncoder = jwtEncoder;
        this.tokenTtlSeconds = properties.getTokenTtlSeconds();
        this.issuer = properties.getIssuer();
        this.audience = properties.getAudience();
    }

    @PostMapping(value = "/oauth2/token", consumes = "application/x-www-form-urlencoded")
    public TokenResponse issueToken(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam(value = "scope", required = false) String requestedScope) {

        if (!"client_credentials".equals(grantType)) {
            throw new OAuth2TokenException(
                    "unsupported_grant_type",
                    "Only the client_credentials grant is supported by this demo",
                    HttpStatus.BAD_REQUEST);
        }

        RegisteredClient client = clientStore.authenticate(clientId, clientSecret)
                .orElseThrow(() -> new OAuth2TokenException(
                        "invalid_client",
                        "Unknown client_id or invalid client_secret",
                        HttpStatus.UNAUTHORIZED));

        Set<String> grantedScopes = resolveScopes(client, requestedScope);
        String scopeClaim = String.join(" ", grantedScopes);

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .audience(List.of(audience))
                .subject(client.clientId())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(tokenTtlSeconds))
                .claim("scope", scopeClaim)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(SignatureAlgorithm.RS256).build();
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

        return new TokenResponse(accessToken, "Bearer", tokenTtlSeconds, scopeClaim);
    }

    private Set<String> resolveScopes(RegisteredClient client, String requestedScope) {
        if (requestedScope == null || requestedScope.isBlank()) {
            return client.scopes();
        }
        Set<String> requested = new LinkedHashSet<>(Arrays.asList(requestedScope.split("\\s+")));
        if (!client.scopes().containsAll(requested)) {
            throw new OAuth2TokenException(
                    "invalid_scope",
                    "Requested scope exceeds what is granted to client " + client.clientId(),
                    HttpStatus.BAD_REQUEST);
        }
        return requested;
    }
}
