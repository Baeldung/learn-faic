package com.baeldung.jiralite;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class CommentAndAuditIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String developerToken;
    private String viewerToken;
    private String outsiderToken;
    private long projectId;
    private long taskId;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = TestHelper.registerAndLogin(mockMvc, objectMapper, "admin", "pass", "ADMIN");
        developerToken = TestHelper.registerAndLogin(mockMvc, objectMapper, "dev", "pass", "DEVELOPER");
        viewerToken = TestHelper.registerAndLogin(mockMvc, objectMapper, "viewer", "pass", "VIEWER");
        outsiderToken = TestHelper.registerAndLogin(mockMvc, objectMapper, "outsider", "pass", "DEVELOPER");

        // Admin creates project and task
        projectId = TestHelper.createProject(mockMvc, objectMapper, adminToken, "Comment Project");

        // Add developer and viewer as members; outsider is NOT added
        long devId = TestHelper.getUserId(mockMvc, objectMapper, adminToken, "dev");
        long viewerId = TestHelper.getUserId(mockMvc, objectMapper, adminToken, "viewer");

        mockMvc.perform(post("/api/projects/" + projectId + "/members")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("userId", devId))))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/projects/" + projectId + "/members")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("userId", viewerId))))
            .andExpect(status().isOk());

        taskId = TestHelper.createTask(mockMvc, objectMapper, developerToken, projectId, "Commented Task");
    }

    @Test
    void projectMember_canAddComment_returns201() throws Exception {
        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + developerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("body", "Dev comment"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.body").value("Dev comment"))
            .andExpect(jsonPath("$.taskId").value(taskId));
    }

    @Test
    void viewer_canAddComment_returns201() throws Exception {
        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + viewerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("body", "Viewer comment"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.body").value("Viewer comment"));
    }

    @Test
    void nonMember_cannotComment_returns403() throws Exception {
        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + outsiderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("body", "Outsider comment"))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").isString());
    }

    @Test
    void listComments_showsAddedComments() throws Exception {
        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + developerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("body", "First comment"))))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + viewerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("body", "Second comment"))))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + developerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].body").value("First comment"))
            .andExpect(jsonPath("$[1].body").value("Second comment"));
    }

    @Test
    void auditByProject_containsExpectedEventTypes() throws Exception {
        // Perform a sequence: task create, status change, comment add
        TestHelper.transition(mockMvc, objectMapper, developerToken, taskId, "IN_PROGRESS");

        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + developerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("body", "Audit comment"))))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/projects/" + projectId + "/audit")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].eventType", hasItem("PROJECT_CREATED")))
            .andExpect(jsonPath("$[*].eventType", hasItem("TASK_CREATED")))
            .andExpect(jsonPath("$[*].eventType", hasItem("TASK_STATUS_CHANGED")))
            .andExpect(jsonPath("$[*].eventType", hasItem("COMMENT_ADDED")));
    }

    @Test
    void auditByTask_containsExpectedEventTypes() throws Exception {
        TestHelper.transition(mockMvc, objectMapper, developerToken, taskId, "IN_PROGRESS");

        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                .header("Authorization", "Bearer " + developerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("body", "Task audit comment"))))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tasks/" + taskId + "/audit")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].eventType", hasItem("TASK_CREATED")))
            .andExpect(jsonPath("$[*].eventType", hasItem("TASK_STATUS_CHANGED")))
            .andExpect(jsonPath("$[*].eventType", hasItem("COMMENT_ADDED")));
    }

    @Test
    void auditLog_containsActorAndDetails() throws Exception {
        mockMvc.perform(get("/api/projects/" + projectId + "/audit")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].actorId").isNumber())
            .andExpect(jsonPath("$[0].details").isString());
    }
}
