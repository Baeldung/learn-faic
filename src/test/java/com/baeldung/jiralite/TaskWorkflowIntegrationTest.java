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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TaskWorkflowIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void validTransitionChainSucceeds() throws Exception {
        String adminToken = registerAndLogin("admin_wf1", "pass", "ADMIN");
        long projectId = createProject(adminToken, "Project A");
        long taskId = createTask(adminToken, projectId, "Task 1");

        transition(adminToken, taskId, "IN_PROGRESS").andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
        transition(adminToken, taskId, "IN_REVIEW").andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("IN_REVIEW")));
        transition(adminToken, taskId, "DONE").andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("DONE")));
        transition(adminToken, taskId, "CLOSED").andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("CLOSED")));
    }

    @Test
    void developerCannotCloseTask() throws Exception {
        String adminToken = registerAndLogin("admin_wf2", "pass", "ADMIN");
        String devToken = registerAndLogin("dev_wf2", "pass", "DEVELOPER");

        long projectId = createProject(adminToken, "Project B");
        addMember(adminToken, projectId, getUserId(adminToken, "dev_wf2"));
        long taskId = createTask(adminToken, projectId, "Task Dev");

        transition(adminToken, taskId, "IN_PROGRESS").andExpect(status().isOk());
        transition(adminToken, taskId, "IN_REVIEW").andExpect(status().isOk());
        transition(adminToken, taskId, "DONE").andExpect(status().isOk());

        transition(devToken, taskId, "CLOSED").andExpect(status().isForbidden());
    }

    @Test
    void managerCanCloseTask() throws Exception {
        String managerToken = registerAndLogin("mgr_wf3", "pass", "MANAGER");
        long projectId = createProject(managerToken, "Project C");
        long taskId = createTask(managerToken, projectId, "Task Mgr");

        transition(managerToken, taskId, "IN_PROGRESS").andExpect(status().isOk());
        transition(managerToken, taskId, "IN_REVIEW").andExpect(status().isOk());
        transition(managerToken, taskId, "DONE").andExpect(status().isOk());
        transition(managerToken, taskId, "CLOSED").andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("CLOSED")));
    }

    @Test
    void reopenAllowedOnlyForManagerAndAdmin() throws Exception {
        String adminToken = registerAndLogin("admin_wf4", "pass", "ADMIN");
        String devToken = registerAndLogin("dev_wf4", "pass", "DEVELOPER");

        long projectId = createProject(adminToken, "Project D");
        addMember(adminToken, projectId, getUserId(adminToken, "dev_wf4"));
        long taskId = createTask(adminToken, projectId, "Task Reopen");

        transition(adminToken, taskId, "IN_PROGRESS").andExpect(status().isOk());
        transition(adminToken, taskId, "IN_REVIEW").andExpect(status().isOk());
        transition(adminToken, taskId, "DONE").andExpect(status().isOk());
        transition(adminToken, taskId, "CLOSED").andExpect(status().isOk());

        transition(devToken, taskId, "OPEN").andExpect(status().isForbidden());
        transition(adminToken, taskId, "OPEN").andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("OPEN")));
    }

    @Test
    void invalidTransitionIsRejected() throws Exception {
        String adminToken = registerAndLogin("admin_wf5", "pass", "ADMIN");
        long projectId = createProject(adminToken, "Project E");
        long taskId = createTask(adminToken, projectId, "Task Invalid");

        transition(adminToken, taskId, "IN_REVIEW").andExpect(status().isBadRequest());
        transition(adminToken, taskId, "DONE").andExpect(status().isBadRequest());
        transition(adminToken, taskId, "CLOSED").andExpect(status().isBadRequest());
    }

    @Test
    void backwardTransitionIsRejected() throws Exception {
        String adminToken = registerAndLogin("admin_wf6", "pass", "ADMIN");
        long projectId = createProject(adminToken, "Project F");
        long taskId = createTask(adminToken, projectId, "Task Backward");

        transition(adminToken, taskId, "IN_PROGRESS").andExpect(status().isOk());
        transition(adminToken, taskId, "OPEN").andExpect(status().isBadRequest());
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
            .andExpect(status().isOk())
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

    private org.springframework.test.web.servlet.ResultActions transition(String token, long taskId,
                                                                          String status) throws Exception {
        return mockMvc.perform(post("/tasks/" + taskId + "/transitions")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of("status", status))));
    }

    private void addMember(String token, long projectId, long userId) throws Exception {
        mockMvc.perform(post("/projects/" + projectId + "/members")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("userId", userId))))
            .andExpect(status().isOk());
    }

    private long getUserId(String token, String username) throws Exception {
        MvcResult result = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/users")
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
