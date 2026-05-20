package com.baeldung.jiralite.audit;

import com.baeldung.jiralite.support.TestFixtures;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuditIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String managerToken;
    private String outsiderToken;
    private Long projectId;
    private Long taskId;

    @BeforeEach
    void setUp() throws Exception {
        managerToken = TestFixtures.registerAndLogin(mockMvc, objectMapper, "amgr", "MANAGER");
        outsiderToken = TestFixtures.registerAndLogin(mockMvc, objectMapper, "aoutsider", "DEVELOPER");
        projectId = TestFixtures.createProject(mockMvc, objectMapper, managerToken, "Audit Project");
        taskId = TestFixtures.createTask(mockMvc, objectMapper, managerToken, projectId, "Audit task");
    }

    @Test
    void creatingTaskWritesTaskCreatedAuditEntry() throws Exception {
        mockMvc.perform(get("/api/projects/" + projectId + "/audit")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.eventType == 'TASK_CREATED')]").isNotEmpty());
    }

    @Test
    void transitioningTaskWritesTaskStatusChangedAuditEntry() throws Exception {
        TestFixtures.transition(mockMvc, managerToken, taskId, "IN_PROGRESS");

        mockMvc.perform(get("/api/tasks/" + taskId + "/audit")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.eventType == 'TASK_STATUS_CHANGED')]").isNotEmpty());
    }

    @Test
    void reopeningTaskWritesTaskReopenedAuditEntry() throws Exception {
        TestFixtures.transition(mockMvc, managerToken, taskId, "IN_PROGRESS");
        TestFixtures.transition(mockMvc, managerToken, taskId, "IN_REVIEW");
        TestFixtures.transition(mockMvc, managerToken, taskId, "DONE");
        TestFixtures.transition(mockMvc, managerToken, taskId, "CLOSED");
        TestFixtures.transition(mockMvc, managerToken, taskId, "OPEN");

        mockMvc.perform(get("/api/tasks/" + taskId + "/audit")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.eventType == 'TASK_REOPENED')]").isNotEmpty());
    }

    @Test
    void addingCommentWritesCommentAddedAuditEntry() throws Exception {
        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\": \"audit test comment\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/projects/" + projectId + "/audit")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.eventType == 'COMMENT_ADDED')]").isNotEmpty());
    }

    @Test
    void memberCanReadProjectAuditLog() throws Exception {
        mockMvc.perform(get("/api/projects/" + projectId + "/audit")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());
    }

    @Test
    void nonMemberCannotReadProjectAuditLog() throws Exception {
        mockMvc.perform(get("/api/projects/" + projectId + "/audit")
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void memberCanReadTaskAuditLog() throws Exception {
        mockMvc.perform(get("/api/tasks/" + taskId + "/audit")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.eventType == 'TASK_CREATED')]").isNotEmpty());
    }
}
