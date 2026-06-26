package com.baeldung.jiralite.user;

import com.baeldung.jiralite.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserRoleIntegrationTest extends IntegrationTestBase {

    @Test
    void adminCanChangeRole() throws Exception {
        long adminId = register("admin1", "secret123");
        setRoleDirectly(adminId, Role.ADMIN);
        long devId = register("dev1", "secret123");
        String adminToken = login("admin1", "secret123");

        mvc.perform(patchAs("/api/users/" + devId + "/role", adminToken, "{\"role\":\"MANAGER\"}"))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));
    }

    @Test
    void nonAdminCannotChangeRole() throws Exception {
        long devId = register("dev2", "secret123");
        long otherId = register("other2", "secret123");
        String devToken = login("dev2", "secret123");

        mvc.perform(patchAs("/api/users/" + otherId + "/role", devToken, "{\"role\":\"MANAGER\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void cannotDemoteLastAdmin() throws Exception {
        long adminId = register("solo", "secret123");
        setRoleDirectly(adminId, Role.ADMIN);
        String adminToken = login("solo", "secret123");

        mvc.perform(patchAs("/api/users/" + adminId + "/role", adminToken, "{\"role\":\"DEVELOPER\"}"))
            .andExpect(status().isConflict());
    }

    @Test
    void adminCanListUsers() throws Exception {
        long adminId = register("admin3", "secret123");
        setRoleDirectly(adminId, Role.ADMIN);
        register("user01", "secret123");
        String adminToken = login("admin3", "secret123");

        mvc.perform(getAs("/api/users", adminToken)).andExpect(status().isOk());
    }

    @Test
    void nonAdminCannotListUsers() throws Exception {
        register("plain", "secret123");
        String token = login("plain", "secret123");
        mvc.perform(getAs("/api/users", token)).andExpect(status().isForbidden());
    }
}
