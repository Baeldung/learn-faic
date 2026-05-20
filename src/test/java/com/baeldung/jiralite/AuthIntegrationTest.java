package com.baeldung.jiralite;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/cleanup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class AuthIntegrationTest {

    private static final String REGISTER_URL = "/auth/register";
    private static final String LOGIN_URL = "/auth/login";
    private static final String ALICE_JSON = "{\"username\":\"alice\",\"password\":\"pass123\"}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void register_returns201_with_token() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(ALICE_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_with_correct_credentials_returns200() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(ALICE_JSON))
            .andExpect(status().isCreated());

        mockMvc.perform(post(LOGIN_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(ALICE_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_with_wrong_password_returns401() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(ALICE_JSON))
            .andExpect(status().isCreated());

        mockMvc.perform(post(LOGIN_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"alice\",\"password\":\"wrongpass\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void protected_endpoint_without_token_returns401() throws Exception {
        mockMvc.perform(get("/users"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void duplicate_username_returns409() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(ALICE_JSON))
            .andExpect(status().isCreated());

        mockMvc.perform(post(REGISTER_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(ALICE_JSON))
            .andExpect(status().isConflict());
    }
}
