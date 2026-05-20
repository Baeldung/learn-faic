package com.baeldung.jiralite;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.baeldung.jiralite.domain.Role;
import com.baeldung.jiralite.domain.User;
import com.baeldung.jiralite.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/cleanup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class CommentAndAuditIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String memberToken;
    private String outsiderToken;
    private Long projectId;
    private Long taskId;

    @BeforeEach
    void setup() throws Exception {
        User member = buildUser("member", Role.DEVELOPER);
        User outsider = buildUser("outsider", Role.DEVELOPER);
        userRepo.save(member);
        userRepo.save(outsider);

        memberToken = login("member");
        outsiderToken = login("outsider");

        String projectResp = mockMvc.perform(post("/projects")
            .header("Authorization", "Bearer " + memberToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"AuditProject\"}"))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        projectId = objectMapper.readTree(projectResp).get("id").asLong();

        String taskResp = mockMvc.perform(post("/tasks")
            .header("Authorization", "Bearer " + memberToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"projectId\":" + projectId + ",\"title\":\"Audit Task\",\"priority\":\"LOW\"}"))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        taskId = objectMapper.readTree(taskResp).get("id").asLong();
    }

    @Test
    void member_can_add_comment() throws Exception {
        mockMvc.perform(post("/tasks/" + taskId + "/comments")
            .header("Authorization", "Bearer " + memberToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"body\":\"A comment\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.body").value("A comment"));
    }

    @Test
    void non_member_cannot_add_comment() throws Exception {
        mockMvc.perform(post("/tasks/" + taskId + "/comments")
            .header("Authorization", "Bearer " + outsiderToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"body\":\"Sneaky comment\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void list_comments_returns_added_comments() throws Exception {
        mockMvc.perform(post("/tasks/" + taskId + "/comments")
            .header("Authorization", "Bearer " + memberToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"body\":\"Hello\"}"))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/tasks/" + taskId + "/comments")
            .header("Authorization", "Bearer " + memberToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].body").value("Hello"));
    }

    @Test
    void project_audit_contains_project_and_task_events() throws Exception {
        mockMvc.perform(get("/projects/" + projectId + "/audit")
            .header("Authorization", "Bearer " + memberToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.eventType=='PROJECT_CREATED')]").exists())
            .andExpect(jsonPath("$[?(@.eventType=='TASK_CREATED')]").exists());
    }

    @Test
    void task_audit_contains_task_created_event() throws Exception {
        mockMvc.perform(get("/tasks/" + taskId + "/audit")
            .header("Authorization", "Bearer " + memberToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.eventType=='TASK_CREATED')]").exists());
    }

    @Test
    void comment_addition_appears_in_task_audit() throws Exception {
        mockMvc.perform(post("/tasks/" + taskId + "/comments")
            .header("Authorization", "Bearer " + memberToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"body\":\"Audit me\"}"))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/tasks/" + taskId + "/audit")
            .header("Authorization", "Bearer " + memberToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.eventType=='COMMENT_ADDED')]").exists());
    }

    private String login(String username) throws Exception {
        String body = "{\"username\":\"" + username + "\",\"password\":\"pass123\"}";
        String resp = mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("token").asText();
    }

    private User buildUser(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("pass123"));
        user.setRole(role);
        return user;
    }
}
