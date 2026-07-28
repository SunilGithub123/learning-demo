package com.learningdemo.oauth2clientcredentials.web;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Publishes this server's public signing key as a standard JWK Set (RFC 7517),
 * the same way a real authorization server would (e.g. Spring Authorization
 * Server's own /oauth2/jwks, or an OIDC provider's jwks_uri). This is what
 * lets a resource server validate tokens without sharing this JVM's beans -
 * a separately deployed API would fetch this endpoint (and cache/refresh the
 * result) instead of hardcoding a public key.
 *
 * toPublicJWK() is essential here: the RSAKey bean also holds the private
 * key used for signing, and serializing that would leak it.
 */
@RestController
public class JwkSetController {

    private final RSAKey rsaKey;

    public JwkSetController(RSAKey rsaKey) {
        this.rsaKey = rsaKey;
    }

    @GetMapping("/oauth2/jwks")
    public Map<String, Object> jwkSet() {
        return new JWKSet(rsaKey.toPublicJWK()).toJSONObject();
    }
}
