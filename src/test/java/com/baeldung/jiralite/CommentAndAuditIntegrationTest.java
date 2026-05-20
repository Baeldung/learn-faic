package com.baeldung.jiralite;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CommentAndAuditIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void projectMemberCanAddAndListComments() throws Exception {
        String adminToken = registerAndLogin("admin_ca1", "pass", "ADMIN");
        String viewerToken = registerAndLogin("viewer_ca1", "pass", "VIEWER");

        long projectId = createProject(adminToken, "Comment Project");
        long viewerId = getUserId(adminToken, "viewer_ca1");
        addMember(adminToken, projectId, viewerId);
        long taskId = createTask(adminToken, projectId, "Commented Task");

        mockMvc.perform(post("/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + viewerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("body", "Hello from viewer"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.body", is("Hello from viewer")))
            .andExpect(jsonPath("$.author.username", is("viewer_ca1")));

        mockMvc.perform(post("/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("body", "Admin comment"))))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].body", is("Hello from viewer")))
            .andExpect(jsonPath("$[1].body", is("Admin comment")));
    }

    @Test
    void nonMemberCannotAddComment() throws Exception {
        String adminToken = registerAndLogin("admin_ca2", "pass", "ADMIN");
        String outsiderToken = registerAndLogin("outsider_ca2", "pass", "DEVELOPER");

        long projectId = createProject(adminToken, "Restricted Project");
        long taskId = createTask(adminToken, projectId, "Task 1");

        mockMvc.perform(post("/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + outsiderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("body", "Should fail"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void projectAuditLogContainsProjectAndTaskEvents() throws Exception {
        String adminToken = registerAndLogin("admin_ca3", "pass", "ADMIN");
        long projectId = createProject(adminToken, "Audited Project");
        long taskId = createTask(adminToken, projectId, "Audited Task");

        mockMvc.perform(get("/projects/" + projectId + "/audit")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
            .andExpect(jsonPath("$[*].eventType", hasItems("PROJECT_CREATED", "TASK_CREATED")));
    }

    @Test
    void taskAuditLogContainsTaskEvents() throws Exception {
        String adminToken = registerAndLogin("admin_ca4", "pass", "ADMIN");
        long projectId = createProject(adminToken, "Task Audit Project");
        long taskId = createTask(adminToken, projectId, "Tracked Task");

        transition(adminToken, taskId, "IN_PROGRESS");

        mockMvc.perform(get("/tasks/" + taskId + "/audit")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
            .andExpect(jsonPath("$[*].eventType", hasItems("TASK_CREATED", "TASK_STATUS_CHANGED")));
    }

    @Test
    void commentAdditionAppearsInTaskAuditLog() throws Exception {
        String adminToken = registerAndLogin("admin_ca5", "pass", "ADMIN");
        long projectId = createProject(adminToken, "Comment Audit Project");
        long taskId = createTask(adminToken, projectId, "Task With Comment");

        mockMvc.perform(post("/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("body", "audit this"))))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/tasks/" + taskId + "/audit")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].eventType", hasItem("COMMENT_ADDED")));
    }

    private String registerAndLogin(String username, String password, String role) throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "username", username, "password", password, "role", role
                ))))
            .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "username", username, "password", password
                ))))
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private long createProject(String token, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/projects")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("name", name))))
            .andExpect(status().isCreated())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createTask(String token, long projectId, String title) throws Exception {
        MvcResult result = mockMvc.perform(post("/tasks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "projectId", projectId, "title", title, "priority", "MEDIUM"
                ))))
            .andExpect(status().isCreated())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private void addMember(String token, long projectId, long userId) throws Exception {
        mockMvc.perform(post("/projects/" + projectId + "/members")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("userId", userId))))
            .andExpect(status().isOk());
    }

    private void transition(String token, long taskId, String status) throws Exception {
        mockMvc.perform(post("/tasks/" + taskId + "/transitions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", status))))
            .andExpect(status().isOk());
    }

    private long getUserId(String token, String username) throws Exception {
        MvcResult result = mockMvc.perform(get("/users")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode users = objectMapper.readTree(result.getResponse().getContentAsString());
        for (JsonNode user : users) {
            if (username.equals(user.get("username").asText())) {
                return user.get("id").asLong();
            }
        }
        throw new IllegalStateException("User not found: " + username);
    }
}
