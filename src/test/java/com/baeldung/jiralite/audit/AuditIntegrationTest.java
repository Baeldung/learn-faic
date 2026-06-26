package com.baeldung.jiralite.audit;

import com.baeldung.jiralite.IntegrationTestBase;
import com.baeldung.jiralite.user.Role;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuditIntegrationTest extends IntegrationTestBase {

    @Test
    void projectCreationProducesAuditEntry() throws Exception {
        long mgrId = register("amgr", "secret123");
        setRoleDirectly(mgrId, Role.MANAGER);
        String token = login("amgr", "secret123");
        long projectId = parse(mvc.perform(postAs("/api/projects", token, "{\"name\":\"P\"}")).andReturn())
                .get("id").asLong();

        MvcResult res = mvc.perform(getAs("/api/projects/" + projectId + "/audit", token))
                .andExpect(status().isOk()).andReturn();
        JsonNode arr = parse(res);
        org.junit.jupiter.api.Assertions.assertTrue(arr.size() >= 1);
        org.junit.jupiter.api.Assertions.assertEquals("PROJECT_CREATED", arr.get(0).get("eventType").asText());
    }

    @Test
    void nonMemberCannotReadProjectAudit() throws Exception {
        long mgrId = register("amgr2", "secret123");
        setRoleDirectly(mgrId, Role.MANAGER);
        String mgrToken = login("amgr2", "secret123");
        long projectId = parse(mvc.perform(postAs("/api/projects", mgrToken, "{\"name\":\"P\"}")).andReturn())
                .get("id").asLong();

        register("outa", "secret123");
        String outToken = login("outa", "secret123");
        mvc.perform(getAs("/api/projects/" + projectId + "/audit", outToken))
                .andExpect(status().isNotFound());
    }
}
