package com.learningdemo.oauth2clientcredentials.config;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * In-memory registry of demo clients. Plain-text secrets are read once from
 * configuration and immediately BCrypt-hashed; only the hash is kept around
 * and used for comparisons. A real deployment would load pre-hashed secrets
 * from a database or secrets manager instead.
 */
@Component
public class RegisteredClientStore {

    private final Map<String, RegisteredClient> clientsById;

    @Autowired
    public RegisteredClientStore(DemoClientsProperties properties, PasswordEncoder passwordEncoder) {
        this.clientsById = properties.getClients().stream()
                .collect(Collectors.toMap(
                        DemoClientsProperties.ClientDefinition::getClientId,
                        c -> new RegisteredClient(
                                c.getClientId(),
                                passwordEncoder.encode(c.getClientSecret()),
                                Set.copyOf(c.getScopes()))));
    }

    public Optional<RegisteredClient> findById(String clientId) {
        return Optional.ofNullable(clientsById.get(clientId));
    }
}
