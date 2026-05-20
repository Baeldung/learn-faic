package com.baeldung.jiralite;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TaskWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String managerToken;
    private String developerToken;
    private long projectId;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = TestHelper.registerAndLogin(mockMvc, objectMapper,
            "admin", "pass", "ADMIN");
        managerToken = TestHelper.registerAndLogin(mockMvc, objectMapper,
            "manager", "pass", "MANAGER");
        developerToken = TestHelper.registerAndLogin(mockMvc, objectMapper,
            "developer", "pass", "DEVELOPER");

        // Admin creates the project (auto-added as member)
        projectId = TestHelper.createProject(mockMvc, objectMapper, adminToken, "Workflow Project");

        // Add manager and developer as members
        long managerId = TestHelper.getUserId(mockMvc, objectMapper, adminToken, "manager");
        long developerId = TestHelper.getUserId(mockMvc, objectMapper, adminToken, "developer");

        mockMvc.perform(post("/api/projects/" + projectId + "/members")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("userId", managerId))))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/projects/" + projectId + "/members")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("userId", developerId))))
            .andExpect(status().isOk());
    }

    @Test
    void happyPath_fullWorkflow_OPEN_to_CLOSED() throws Exception {
        long taskId = TestHelper.createTask(mockMvc, objectMapper, developerToken,
            projectId, "Full workflow task");

        // OPEN -> IN_PROGRESS
        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                .header("Authorization", "Bearer " + developerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", "IN_PROGRESS"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // IN_PROGRESS -> IN_REVIEW
        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                .header("Authorization", "Bearer " + developerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", "IN_REVIEW"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("IN_REVIEW"));

        // IN_REVIEW -> DONE
        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                .header("Authorization", "Bearer " + developerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", "DONE"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DONE"));

        // DONE -> CLOSED (manager can do this)
        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", "CLOSED"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void invalidTransition_OPEN_to_DONE_returns409() throws Exception {
        long taskId = TestHelper.createTask(mockMvc, objectMapper, developerToken,
            projectId, "Invalid skip task");

        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                .header("Authorization", "Bearer " + developerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", "DONE"))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").isString());
    }

    @Test
    void invalidTransition_IN_PROGRESS_to_CLOSED_returns409() throws Exception {
        long taskId = TestHelper.createTask(mockMvc, objectMapper, developerToken,
            projectId, "Bad close task");

        TestHelper.transition(mockMvc, objectMapper, developerToken, taskId, "IN_PROGRESS");

        // Even a manager can't skip the forward path: must go IN_REVIEW -> DONE -> CLOSED.
        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", "CLOSED"))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").isString());
    }

    @Test
    void closingByDeveloper_returns403() throws Exception {
        long taskId = TestHelper.createTask(mockMvc, objectMapper, developerToken,
            projectId, "Close by dev task");

        TestHelper.transition(mockMvc, objectMapper, developerToken, taskId, "IN_PROGRESS");
        TestHelper.transition(mockMvc, objectMapper, developerToken, taskId, "IN_REVIEW");
        TestHelper.transition(mockMvc, objectMapper, developerToken, taskId, "DONE");

        // Developer tries to close — should be 403
        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                .header("Authorization", "Bearer " + developerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", "CLOSED"))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").isString());
    }

    @Test
    void closingByManager_returns200() throws Exception {
        long taskId = TestHelper.createTask(mockMvc, objectMapper, developerToken,
            projectId, "Close by manager task");

        TestHelper.transition(mockMvc, objectMapper, developerToken, taskId, "IN_PROGRESS");
        TestHelper.transition(mockMvc, objectMapper, developerToken, taskId, "IN_REVIEW");
        TestHelper.transition(mockMvc, objectMapper, developerToken, taskId, "DONE");

        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", "CLOSED"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void reopen_CLOSED_to_OPEN_byManager_returns200() throws Exception {
        long taskId = TestHelper.createTask(mockMvc, objectMapper, developerToken,
            projectId, "Reopen task");

        TestHelper.transition(mockMvc, objectMapper, developerToken, taskId, "IN_PROGRESS");
        TestHelper.transition(mockMvc, objectMapper, developerToken, taskId, "IN_REVIEW");
        TestHelper.transition(mockMvc, objectMapper, developerToken, taskId, "DONE");
        TestHelper.transition(mockMvc, objectMapper, managerToken, taskId, "CLOSED");

        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", "OPEN"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void reopen_CLOSED_to_OPEN_byDeveloper_returns403() throws Exception {
        long taskId = TestHelper.createTask(mockMvc, objectMapper, developerToken,
            projectId, "Reopen denied task");

        TestHelper.transition(mockMvc, objectMapper, developerToken, taskId, "IN_PROGRESS");
        TestHelper.transition(mockMvc, objectMapper, developerToken, taskId, "IN_REVIEW");
        TestHelper.transition(mockMvc, objectMapper, developerToken, taskId, "DONE");
        TestHelper.transition(mockMvc, objectMapper, managerToken, taskId, "CLOSED");

        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                .header("Authorization", "Bearer " + developerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", "OPEN"))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").isString());
    }

    @Test
    void reopen_CLOSED_to_OPEN_byAdmin_returns200() throws Exception {
        long taskId = TestHelper.createTask(mockMvc, objectMapper, adminToken,
            projectId, "Admin reopen task");

        TestHelper.transition(mockMvc, objectMapper, adminToken, taskId, "IN_PROGRESS");
        TestHelper.transition(mockMvc, objectMapper, adminToken, taskId, "IN_REVIEW");
        TestHelper.transition(mockMvc, objectMapper, adminToken, taskId, "DONE");
        TestHelper.transition(mockMvc, objectMapper, adminToken, taskId, "CLOSED");

        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", "OPEN"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("OPEN"));
    }
}
