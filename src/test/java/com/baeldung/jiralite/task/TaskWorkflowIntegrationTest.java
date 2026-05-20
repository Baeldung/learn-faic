package com.baeldung.jiralite.task;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TaskWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String managerToken;
    private String developerToken;
    private Long projectId;

    @BeforeEach
    void setUp() throws Exception {
        managerToken = TestFixtures.registerAndLogin(mockMvc, objectMapper, "mgr", "MANAGER");
        developerToken = TestFixtures.registerAndLogin(mockMvc, objectMapper, "dev", "DEVELOPER");
        projectId = TestFixtures.createProject(mockMvc, objectMapper, managerToken, "Workflow Project");

        Long devId = TestFixtures.getUserId(mockMvc, objectMapper, managerToken, "dev");
        TestFixtures.addMember(mockMvc, managerToken, projectId, devId);
    }

    @Test
    void newTaskHasOpenStatus() throws Exception {
        Long taskId = TestFixtures.createTask(mockMvc, objectMapper, managerToken, projectId, "New task");

        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void managerCanRunFullWorkflowOpenThroughClosed() throws Exception {
        Long taskId = TestFixtures.createTask(mockMvc, objectMapper, managerToken, projectId, "Full flow");

        TestFixtures.transition(mockMvc, managerToken, taskId, "IN_PROGRESS");
        TestFixtures.transition(mockMvc, managerToken, taskId, "IN_REVIEW");
        TestFixtures.transition(mockMvc, managerToken, taskId, "DONE");

        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"CLOSED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void transitionFromOpenToDoneIsRejected() throws Exception {
        Long taskId = TestFixtures.createTask(mockMvc, objectMapper, managerToken, projectId, "Skip task");

        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"DONE\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void transitionFromInProgressToClosedIsRejected() throws Exception {
        Long taskId = TestFixtures.createTask(mockMvc, objectMapper, managerToken, projectId, "Skip close");
        TestFixtures.transition(mockMvc, managerToken, taskId, "IN_PROGRESS");

        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"CLOSED\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void developerCannotCloseTask() throws Exception {
        Long taskId = TestFixtures.createTask(mockMvc, objectMapper, managerToken, projectId, "Dev close");
        TestFixtures.transition(mockMvc, managerToken, taskId, "IN_PROGRESS");
        TestFixtures.transition(mockMvc, managerToken, taskId, "IN_REVIEW");
        TestFixtures.transition(mockMvc, managerToken, taskId, "DONE");

        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                        .header("Authorization", "Bearer " + developerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"CLOSED\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void developerCannotReopenTask() throws Exception {
        Long taskId = TestFixtures.createTask(mockMvc, objectMapper, managerToken, projectId, "Dev reopen");
        TestFixtures.transition(mockMvc, managerToken, taskId, "IN_PROGRESS");
        TestFixtures.transition(mockMvc, managerToken, taskId, "IN_REVIEW");
        TestFixtures.transition(mockMvc, managerToken, taskId, "DONE");
        TestFixtures.transition(mockMvc, managerToken, taskId, "CLOSED");

        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                        .header("Authorization", "Bearer " + developerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"OPEN\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCanCloseTask() throws Exception {
        Long taskId = TestFixtures.createTask(mockMvc, objectMapper, managerToken, projectId, "Mgr close");
        TestFixtures.transition(mockMvc, managerToken, taskId, "IN_PROGRESS");
        TestFixtures.transition(mockMvc, managerToken, taskId, "IN_REVIEW");
        TestFixtures.transition(mockMvc, managerToken, taskId, "DONE");

        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"CLOSED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void managerCanReopenClosedTask() throws Exception {
        Long taskId = TestFixtures.createTask(mockMvc, objectMapper, managerToken, projectId, "Mgr reopen");
        TestFixtures.transition(mockMvc, managerToken, taskId, "IN_PROGRESS");
        TestFixtures.transition(mockMvc, managerToken, taskId, "IN_REVIEW");
        TestFixtures.transition(mockMvc, managerToken, taskId, "DONE");
        TestFixtures.transition(mockMvc, managerToken, taskId, "CLOSED");

        mockMvc.perform(post("/api/tasks/" + taskId + "/transition")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"OPEN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"));
    }
}
