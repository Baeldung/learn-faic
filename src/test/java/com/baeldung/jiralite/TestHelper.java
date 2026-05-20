package com.baeldung.jiralite;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

final class TestHelper {

    private TestHelper() {
    }

    static String registerAndLogin(MockMvc mockMvc, ObjectMapper objectMapper,
            String username, String password, String role) throws Exception {
        Map<String, String> regBody = role != null
            ? Map.of("username", username, "password", password, "role", role)
            : Map.of("username", username, "password", password);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regBody)))
            .andExpect(status().isCreated());

        return login(mockMvc, objectMapper, username, password);
    }

    static String login(MockMvc mockMvc, ObjectMapper objectMapper,
            String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "username", username,
                    "password", password
                ))))
            .andExpect(status().isOk())
            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
            .get("token").asText();
    }

    static long createProject(MockMvc mockMvc, ObjectMapper objectMapper,
            String token, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/projects")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", name,
                    "description", "Test project " + name
                ))))
            .andExpect(status().isCreated())
            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
            .get("id").asLong();
    }

    static long createTask(MockMvc mockMvc, ObjectMapper objectMapper,
            String token, long projectId, String title) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "projectId", projectId,
                    "title", title
                ))))
            .andExpect(status().isCreated())
            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
            .get("id").asLong();
    }

    static void transition(MockMvc mockMvc, ObjectMapper objectMapper,
            String token, long taskId, String status) throws Exception {
        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", status))))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    static long getUserId(MockMvc mockMvc, ObjectMapper objectMapper,
            String adminToken, String username) throws Exception {
        MvcResult result = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/users")
                    .header("Authorization", "Bearer " + adminToken))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andReturn();

        com.fasterxml.jackson.databind.JsonNode users =
            objectMapper.readTree(result.getResponse().getContentAsString());

        for (com.fasterxml.jackson.databind.JsonNode user : users) {
            if (username.equals(user.get("username").asText())) {
                return user.get("id").asLong();
            }
        }
        throw new IllegalStateException("User not found: " + username);
    }
}
