package com.baeldung.jiralite;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class TaskWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String managerToken;
    private String developerToken;
    private Long taskId;

    @BeforeEach
    void setup() throws Exception {
        User manager = buildUser("mgr", Role.MANAGER);
        User developer = buildUser("dev", Role.DEVELOPER);
        userRepo.save(manager);
        userRepo.save(developer);

        managerToken = login("mgr");
        developerToken = login("dev");

        String projectResp = mockMvc.perform(post("/projects")
            .header("Authorization", "Bearer " + managerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"P\"}"))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long projectId = objectMapper.readTree(projectResp).get("id").asLong();

        mockMvc.perform(post("/projects/" + projectId + "/members")
            .header("Authorization", "Bearer " + managerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"userId\":" + developer.getId() + "}"))
            .andExpect(status().isOk());

        String taskResp = mockMvc.perform(post("/tasks")
            .header("Authorization", "Bearer " + managerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"projectId\":" + projectId + ",\"title\":\"T\",\"priority\":\"MEDIUM\"}"))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        taskId = objectMapper.readTree(taskResp).get("id").asLong();
    }

    @Test
    void valid_transitions_succeed() throws Exception {
        doTransition(taskId, "IN_PROGRESS", managerToken, status().isOk());
        doTransition(taskId, "IN_REVIEW", managerToken, status().isOk());
        doTransition(taskId, "DONE", managerToken, status().isOk());
    }

    @Test
    void developer_cannot_close_task() throws Exception {
        doTransition(taskId, "IN_PROGRESS", managerToken, status().isOk());
        doTransition(taskId, "IN_REVIEW", managerToken, status().isOk());
        doTransition(taskId, "DONE", managerToken, status().isOk());
        doTransition(taskId, "CLOSED", developerToken, status().isForbidden());
    }

    @Test
    void manager_can_close_task() throws Exception {
        doTransition(taskId, "IN_PROGRESS", managerToken, status().isOk());
        doTransition(taskId, "IN_REVIEW", managerToken, status().isOk());
        doTransition(taskId, "DONE", managerToken, status().isOk());
        doTransition(taskId, "CLOSED", managerToken, status().isOk());
    }

    @Test
    void developer_cannot_reopen_task() throws Exception {
        doTransition(taskId, "IN_PROGRESS", managerToken, status().isOk());
        doTransition(taskId, "IN_REVIEW", managerToken, status().isOk());
        doTransition(taskId, "DONE", managerToken, status().isOk());
        doTransition(taskId, "CLOSED", managerToken, status().isOk());
        doTransition(taskId, "OPEN", developerToken, status().isForbidden());
    }

    @Test
    void manager_can_reopen_task() throws Exception {
        doTransition(taskId, "IN_PROGRESS", managerToken, status().isOk());
        doTransition(taskId, "IN_REVIEW", managerToken, status().isOk());
        doTransition(taskId, "DONE", managerToken, status().isOk());
        doTransition(taskId, "CLOSED", managerToken, status().isOk());
        doTransition(taskId, "OPEN", managerToken, status().isOk());
    }

    @Test
    void skipping_status_returns400() throws Exception {
        doTransition(taskId, "DONE", managerToken, status().isBadRequest());
    }

    @Test
    void backward_transition_returns400() throws Exception {
        doTransition(taskId, "IN_PROGRESS", managerToken, status().isOk());
        doTransition(taskId, "OPEN", managerToken, status().isBadRequest());
    }

    private void doTransition(Long tid, String taskStatus, String token,
        org.springframework.test.web.servlet.ResultMatcher expected) throws Exception {
        mockMvc.perform(post("/tasks/" + tid + "/transitions")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"" + taskStatus + "\"}"))
            .andExpect(expected);
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
