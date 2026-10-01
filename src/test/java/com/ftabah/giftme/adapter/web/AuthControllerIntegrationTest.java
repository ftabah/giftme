package com.ftabah.giftme.adapter.web;

import com.ftabah.giftme.adapter.storage.csv.AccountActionTokenStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "giftme.storage.data-directory=target/auth-integration",
        "giftme.email.require-verification=false",
        "giftme.email.smtp-enabled=false"
})
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
        @Autowired
        private AccountActionTokenStore actionTokens;

        @Test
        void servesThePasswordResetPageWithoutAuthentication() throws Exception {
                mockMvc.perform(get("/reset-password").queryParam("token", "test-token"))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("id=\"reset-form\"")));
        }

        @Test
        void verifiesEmailAndServesFiveSecondLoginConfirmation() throws Exception {
                String token = actionTokens.issue(UUID.randomUUID(), "VERIFY", Instant.now().plusSeconds(60));

                mockMvc.perform(get("/api/auth/verify-email").queryParam("token", token))
                                .andExpect(status().isOk())
                                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                                .andExpect(content().string(containsString("5;url=/?emailVerified=1")));
        }

    @Test
    void registersAndLogsInWithAValidCredential() throws Exception {
        String email = "user-" + System.nanoTime() + "@example.com";
        String body = "{\"email\":\"" + email + "\",\"password\":\"secret123\"}";

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.emailVerificationRequired").value(false))
                .andExpect(jsonPath("$.token").doesNotExist());
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    void rejectsShortPasswordsAndInvalidCredentials() throws Exception {
        String email = "invalid-" + System.nanoTime() + "@example.com";
        String shortBody = "{\"email\":\"" + email + "\",\"password\":\"short\"}";
        String wrongBody = "{\"email\":\"missing@example.com\",\"password\":\"secret123\"}";

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(shortBody))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(wrongBody))
                .andExpect(status().isUnauthorized());
    }
}