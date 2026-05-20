package com.baeldung.jiralite.user;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserRoleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String developerToken;
    private Long targetUserId;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = TestFixtures.registerAndLogin(mockMvc, objectMapper, "radmin", "ADMIN");
        developerToken = TestFixtures.registerAndLogin(mockMvc, objectMapper, "rdev", "DEVELOPER");
        TestFixtures.registerAndLogin(mockMvc, objectMapper, "rtarget", "DEVELOPER");
        targetUserId = TestFixtures.getUserId(mockMvc, objectMapper, adminToken, "rtarget");
    }

    @Test
    void adminCanChangeUserRole() throws Exception {
        mockMvc.perform(put("/api/users/" + targetUserId + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": \"MANAGER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MANAGER"));
    }

    @Test
    void developerCannotChangeUserRole() throws Exception {
        mockMvc.perform(put("/api/users/" + targetUserId + "/role")
                        .header("Authorization", "Bearer " + developerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": \"MANAGER\"}"))
                .andExpect(status().isForbidden());
    }
}
