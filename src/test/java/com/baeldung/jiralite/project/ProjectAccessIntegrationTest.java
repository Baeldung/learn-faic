package com.baeldung.jiralite.project;

import com.baeldung.jiralite.IntegrationTestBase;
import com.baeldung.jiralite.user.Role;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProjectAccessIntegrationTest extends IntegrationTestBase {

    @Test
    void managerCanCreateProjectAndListsIt() throws Exception {
        long managerId = register("mgr", "secret123");
        setRoleDirectly(managerId, Role.MANAGER);
        String token = login("mgr", "secret123");

        MvcResult created = mvc.perform(postAs("/api/projects", token, "{\"name\":\"Apollo\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Apollo"))
                .andReturn();
        long projectId = parse(created).get("id").asLong();

        mvc.perform(getAs("/api/projects", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(projectId));
    }

    @Test
    void developerCannotCreateProject() throws Exception {
        register("dev", "secret123");
        String token = login("dev", "secret123");
        mvc.perform(postAs("/api/projects", token, "{\"name\":\"Apollo\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void nonMemberSees404OnProject() throws Exception {
        long managerId = register("mgr2", "secret123");
        setRoleDirectly(managerId, Role.MANAGER);
        String mgrToken = login("mgr2", "secret123");
        long projectId = parse(mvc.perform(postAs("/api/projects", mgrToken, "{\"name\":\"Secret\"}"))
                .andReturn()).get("id").asLong();

        register("outsider", "secret123");
        String outsiderToken = login("outsider", "secret123");

        mvc.perform(getAs("/api/projects/" + projectId, outsiderToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void nonMemberListShowsNoOtherProjects() throws Exception {
        long managerId = register("mgr3", "secret123");
        setRoleDirectly(managerId, Role.MANAGER);
        String mgrToken = login("mgr3", "secret123");
        mvc.perform(postAs("/api/projects", mgrToken, "{\"name\":\"Hidden\"}")).andExpect(status().isCreated());

        register("outsider2", "secret123");
        String outsiderToken = login("outsider2", "secret123");

        MvcResult res = mvc.perform(getAs("/api/projects", outsiderToken))
                .andExpect(status().isOk()).andReturn();
        JsonNode arr = parse(res);
        org.junit.jupiter.api.Assertions.assertEquals(0, arr.size());
    }

    @Test
    void adminSeesAllProjects() throws Exception {
        long managerId = register("mgr4", "secret123");
        setRoleDirectly(managerId, Role.MANAGER);
        String mgrToken = login("mgr4", "secret123");
        mvc.perform(postAs("/api/projects", mgrToken, "{\"name\":\"Cross\"}")).andExpect(status().isCreated());

        long adminId = register("adm4", "secret123");
        setRoleDirectly(adminId, Role.ADMIN);
        String adminToken = login("adm4", "secret123");

        MvcResult res = mvc.perform(getAs("/api/projects", adminToken)).andExpect(status().isOk()).andReturn();
        org.junit.jupiter.api.Assertions.assertTrue(parse(res).size() >= 1);
    }

    @Test
    void managerCanAddAndRemoveMember() throws Exception {
        long managerId = register("mgr5", "secret123");
        setRoleDirectly(managerId, Role.MANAGER);
        String mgrToken = login("mgr5", "secret123");
        long projectId = parse(mvc.perform(postAs("/api/projects", mgrToken, "{\"name\":\"P\"}"))
                .andReturn()).get("id").asLong();
        long devId = register("teammate", "secret123");

        mvc.perform(postAs("/api/projects/" + projectId + "/members", mgrToken, "{\"userId\":" + devId + "}"))
                .andExpect(status().isOk());

        mvc.perform(deleteAs("/api/projects/" + projectId + "/members/" + devId, mgrToken))
                .andExpect(status().isNoContent());
    }
}
