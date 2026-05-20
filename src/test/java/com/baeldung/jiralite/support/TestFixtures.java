package com.baeldung.jiralite.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Reusable helpers for creating users and acquiring JWTs inside integration tests.
 */
public final class TestFixtures {

    private TestFixtures() {
    }

    public static String registerAndLogin(MockMvc mockMvc, ObjectMapper objectMapper,
            String username, String role) throws Exception {
        String registerBody = """
                {"username": "%s", "password": "password123", "role": "%s"}
                """.formatted(username, role);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated());

        return login(mockMvc, objectMapper, username);
    }

    public static String login(MockMvc mockMvc, ObjectMapper objectMapper, String username) throws Exception {
        String loginBody = """
                {"username": "%s", "password": "password123"}
                """.formatted(username);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        return objectMapper.readTree(json).get("token").asText();
    }

    public static Long createProject(MockMvc mockMvc, ObjectMapper objectMapper,
            String token, String name) throws Exception {
        String body = """
                {"name": "%s", "description": "test project"}
                """.formatted(name);

        MvcResult result = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    public static Long createTask(MockMvc mockMvc, ObjectMapper objectMapper,
            String token, Long projectId, String title) throws Exception {
        String body = """
                {"projectId": %d, "title": "%s", "priority": "MEDIUM"}
                """.formatted(projectId, title);

        MvcResult result = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    public static void transition(MockMvc mockMvc, String token, Long taskId, String status)
            throws Exception {
        String body = """
                {"status": "%s"}
                """.formatted(status);

        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    public static Long getUserId(MockMvc mockMvc, ObjectMapper objectMapper, String token,
            String username) throws Exception {
        MvcResult result = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/users")
                                .header("Authorization", "Bearer " + token))
                .andReturn();

        com.fasterxml.jackson.databind.JsonNode users =
                objectMapper.readTree(result.getResponse().getContentAsString());
        for (com.fasterxml.jackson.databind.JsonNode u : users) {
            if (username.equals(u.get("username").asText())) {
                return u.get("id").asLong();
            }
        }
        throw new IllegalStateException("User not found: " + username);
    }

    public static void addMember(MockMvc mockMvc, String token, Long projectId, Long userId)
            throws Exception {
        String body = """
                {"userId": %d}
                """.formatted(userId);

        mockMvc.perform(post("/api/projects/" + projectId + "/members")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
