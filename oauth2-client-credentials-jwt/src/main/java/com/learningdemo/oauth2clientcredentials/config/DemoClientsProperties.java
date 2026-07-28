package com.learningdemo.oauth2clientcredentials.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the demo client registry from application.yml (demo.oauth2.*).
 */
@ConfigurationProperties(prefix = "demo.oauth2")
public class DemoClientsProperties {

    private long tokenTtlSeconds = 300;
    private String issuer = "https://learning-demo/oauth2-client-credentials-jwt";
    private String audience = "learning-demo-api";
    private List<ClientDefinition> clients = List.of();

    public long getTokenTtlSeconds() {
        return tokenTtlSeconds;
    }

    public void setTokenTtlSeconds(long tokenTtlSeconds) {
        this.tokenTtlSeconds = tokenTtlSeconds;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public List<ClientDefinition> getClients() {
        return clients;
    }

    public void setClients(List<ClientDefinition> clients) {
        this.clients = clients;
    }

    public static class ClientDefinition {
        private String clientId;
        private String clientSecret;
        private List<String> scopes = List.of();

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public List<String> getScopes() {
            return scopes;
        }

        public void setScopes(List<String> scopes) {
            this.scopes = scopes;
        }
    }
}
