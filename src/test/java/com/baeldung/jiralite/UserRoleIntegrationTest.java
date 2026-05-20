package com.baeldung.jiralite;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class UserRoleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String managerToken;
    private String developerToken;
    private long targetUserId;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = TestHelper.registerAndLogin(mockMvc, objectMapper, "admin", "pass", "ADMIN");
        managerToken = TestHelper.registerAndLogin(mockMvc, objectMapper, "manager", "pass", "MANAGER");
        developerToken = TestHelper.registerAndLogin(mockMvc, objectMapper, "targetDev", "pass", "DEVELOPER");
        targetUserId = TestHelper.getUserId(mockMvc, objectMapper, adminToken, "targetDev");
    }

    @Test
    void changeRole_byAdmin_returns200() throws Exception {
        mockMvc.perform(patch("/api/users/" + targetUserId + "/role")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("role", "MANAGER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("MANAGER"))
            .andExpect(jsonPath("$.username").value("targetDev"));
    }

    @Test
    void changeRole_byManager_returns403() throws Exception {
        mockMvc.perform(patch("/api/users/" + targetUserId + "/role")
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("role", "VIEWER"))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").isString());
    }

    @Test
    void changeRole_byDeveloper_returns403() throws Exception {
        mockMvc.perform(patch("/api/users/" + targetUserId + "/role")
                .header("Authorization", "Bearer " + developerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("role", "MANAGER"))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").isString());
    }

    @Test
    void changeRole_withoutToken_returns4xx() throws Exception {
        mockMvc.perform(patch("/api/users/" + targetUserId + "/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("role", "MANAGER"))))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void changeRole_toAllRoles_byAdmin_succeeds() throws Exception {
        for (String role : new String[]{"VIEWER", "DEVELOPER", "MANAGER", "ADMIN"}) {
            mockMvc.perform(patch("/api/users/" + targetUserId + "/role")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("role", role))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value(role));
        }
    }
}
