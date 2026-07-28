package com.learningdemo.oauth2clientcredentials;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Exercises the full client_credentials flow through the real servlet
 * filter chain (Spring Security included), without opening real sockets.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OAuth2ClientCredentialsFlowTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void unauthenticatedRequestToProtectedResourceIsRejected() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidClientCredentialsAreRejected() throws Exception {
        requestToken("demo-client", "wrong-secret", null)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_client"));
    }

    @Test
    void unknownGrantTypeIsRejected() throws Exception {
        mockMvc.perform(post("/oauth2/token")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "password")
                        .param("client_id", "demo-client")
                        .param("client_secret", "demo-secret"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("unsupported_grant_type"));
    }

    @Test
    void clientWithFullScopeCanReadAndWriteOrders() throws Exception {
        String accessToken = issueAccessToken("demo-client", "demo-secret", null);

        mockMvc.perform(get("/api/orders").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Mechanical keyboard")));

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"item\":\"Monitor\",\"quantity\":1}"))
                .andExpect(status().isCreated());
    }

    @Test
    void readOnlyClientCannotCreateOrders() throws Exception {
        String accessToken = issueAccessToken("readonly-client", "readonly-secret", null);

        mockMvc.perform(get("/api/orders").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"item\":\"Monitor\",\"quantity\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void requestingScopeBeyondClientGrantIsRejected() throws Exception {
        requestToken("readonly-client", "readonly-secret", "orders.write")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_scope"));
    }

    private String issueAccessToken(String clientId, String clientSecret, String scope) throws Exception {
        MvcResult result = requestToken(clientId, clientSecret, scope)
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("access_token").asText();
    }

    private org.springframework.test.web.servlet.ResultActions requestToken(String clientId, String clientSecret, String scope) throws Exception {
        var request = post("/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("grant_type", "client_credentials")
                .param("client_id", clientId)
                .param("client_secret", clientSecret);
        if (scope != null) {
            request.param("scope", scope);
        }
        return mockMvc.perform(request);
    }
}
