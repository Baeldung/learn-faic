package com.baeldung.jiralite.comment;

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
class CommentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String managerToken;
    private String viewerToken;
    private String outsiderToken;
    private Long projectId;
    private Long taskId;

    @BeforeEach
    void setUp() throws Exception {
        managerToken = TestFixtures.registerAndLogin(mockMvc, objectMapper, "cmgr", "MANAGER");
        viewerToken = TestFixtures.registerAndLogin(mockMvc, objectMapper, "cviewer", "VIEWER");
        outsiderToken = TestFixtures.registerAndLogin(mockMvc, objectMapper, "coutsider", "DEVELOPER");

        projectId = TestFixtures.createProject(mockMvc, objectMapper, managerToken, "Comment Project");
        Long viewerId = TestFixtures.getUserId(mockMvc, objectMapper, managerToken, "cviewer");
        TestFixtures.addMember(mockMvc, managerToken, projectId, viewerId);

        taskId = TestFixtures.createTask(mockMvc, objectMapper, managerToken, projectId, "Task for comments");
    }

    @Test
    void projectMemberCanAddComment() throws Exception {
        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\": \"A comment from the manager\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.body").value("A comment from the manager"));
    }

    @Test
    void viewerWhoIsMemberCanAddComment() throws Exception {
        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\": \"A viewer comment\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.body").value("A viewer comment"));
    }

    @Test
    void nonMemberCannotAddComment() throws Exception {
        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                        .header("Authorization", "Bearer " + outsiderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\": \"Outsider comment\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listCommentsReturnsInCreatedAtOrder() throws Exception {
        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\": \"First comment\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\": \"Second comment\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tasks/" + taskId + "/comments")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].body").value("First comment"))
                .andExpect(jsonPath("$[1].body").value("Second comment"));
    }
}
