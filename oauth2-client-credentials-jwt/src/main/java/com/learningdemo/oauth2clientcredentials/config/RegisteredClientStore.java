package com.learningdemo.oauth2clientcredentials.config;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
    private final PasswordEncoder passwordEncoder;

    // A precomputed hash checked when client_id doesn't exist, so an unknown
    // client_id takes the same code path (and roughly the same time) as a
    // known client_id with a wrong secret. Without this, response latency
    // would leak which client_ids are registered.
    private final String dummyHash;

    @Autowired
    public RegisteredClientStore(DemoClientsProperties properties, PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.clientsById = properties.getClients().stream()
                .collect(Collectors.toMap(
                        DemoClientsProperties.ClientDefinition::getClientId,
                        c -> new RegisteredClient(
                                c.getClientId(),
                                passwordEncoder.encode(c.getClientSecret()),
                                Set.copyOf(c.getScopes()))));
        this.dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    /**
     * Verifies a client_id/client_secret pair. Always runs a BCrypt comparison,
     * even for an unknown client_id, to avoid leaking client existence via
     * timing.
     */
    public Optional<RegisteredClient> authenticate(String clientId, String clientSecret) {
        RegisteredClient client = clientsById.get(clientId);
        String hashToCheck = client != null ? client.hashedSecret() : dummyHash;
        boolean secretMatches = passwordEncoder.matches(clientSecret, hashToCheck);
        return (client != null && secretMatches) ? Optional.of(client) : Optional.empty();
    }
}
