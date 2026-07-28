package com.learningdemo.oauth2clientcredentials.config;

import java.util.Set;

/**
 * A client allowed to request tokens via the client_credentials grant.
 * The secret is stored already BCrypt-hashed; see {@link RegisteredClientStore}.
 */
public record RegisteredClient(String clientId, String hashedSecret, Set<String> scopes) {
}
