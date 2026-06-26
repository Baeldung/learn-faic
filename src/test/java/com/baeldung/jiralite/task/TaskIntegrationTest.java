package com.baeldung.jiralite.task;

import com.baeldung.jiralite.IntegrationTestBase;
import com.baeldung.jiralite.user.Role;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskIntegrationTest extends IntegrationTestBase {

    private long createProjectAs(String token, String name) throws Exception {
        return parse(mvc.perform(postAs("/api/projects", token, "{\"name\":\"" + name + "\"}")).andReturn())
            .get("id").asLong();
    }

    @Test
    void memberCanCreateTask() throws Exception {
        long mgrId = register("tmgr", "secret123");
        setRoleDirectly(mgrId, Role.MANAGER);
        String mgrToken = login("tmgr", "secret123");
        long projectId = createProjectAs(mgrToken, "P");

        mvc.perform(postAs("/api/tasks", mgrToken,
            "{\"projectId\":" + projectId + ",\"title\":\"T1\",\"priority\":\"HIGH\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void nonMemberCannotCreateTaskInProject() throws Exception {
        long mgrId = register("tmgr2", "secret123");
        setRoleDirectly(mgrId, Role.MANAGER);
        String mgrToken = login("tmgr2", "secret123");
        long projectId = createProjectAs(mgrToken, "P");

        register("outsider3", "secret123");
        String outsiderToken = login("outsider3", "secret123");

        mvc.perform(postAs("/api/tasks", outsiderToken,
            "{\"projectId\":" + projectId + ",\"title\":\"T\",\"priority\":\"LOW\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void invalidTransitionReturns409() throws Exception {
        long mgrId = register("tmgr3", "secret123");
        setRoleDirectly(mgrId, Role.MANAGER);
        String mgrToken = login("tmgr3", "secret123");
        long projectId = createProjectAs(mgrToken, "P");
        long taskId = parse(mvc.perform(postAs("/api/tasks", mgrToken,
            "{\"projectId\":" + projectId + ",\"title\":\"T\",\"priority\":\"LOW\"}")).andReturn())
            .get("id").asLong();

        mvc.perform(postAs("/api/tasks/" + taskId + "/transition", mgrToken, "{\"status\":\"DONE\"}"))
            .andExpect(status().isConflict());
    }

    @Test
    void fullWorkflowSucceeds() throws Exception {
        long mgrId = register("tmgr4", "secret123");
        setRoleDirectly(mgrId, Role.MANAGER);
        String mgrToken = login("tmgr4", "secret123");
        long projectId = createProjectAs(mgrToken, "P");
        long taskId = parse(mvc.perform(postAs("/api/tasks", mgrToken,
            "{\"projectId\":" + projectId + ",\"title\":\"T\",\"priority\":\"LOW\"}")).andReturn())
            .get("id").asLong();

        mvc.perform(postAs("/api/tasks/" + taskId + "/transition", mgrToken, "{\"status\":\"IN_PROGRESS\"}"))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));
        mvc.perform(postAs("/api/tasks/" + taskId + "/transition", mgrToken, "{\"status\":\"IN_REVIEW\"}"))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));
        mvc.perform(postAs("/api/tasks/" + taskId + "/transition", mgrToken, "{\"status\":\"DONE\"}"))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));
        mvc.perform(postAs("/api/tasks/" + taskId + "/transition", mgrToken, "{\"status\":\"CLOSED\"}"))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));
    }

    @Test
    void developerCannotClose() throws Exception {
        long mgrId = register("tmgr5", "secret123");
        setRoleDirectly(mgrId, Role.MANAGER);
        String mgrToken = login("tmgr5", "secret123");
        long projectId = createProjectAs(mgrToken, "P");
        long devId = register("tdev5", "secret123");
        mvc.perform(postAs("/api/projects/" + projectId + "/members", mgrToken, "{\"userId\":" + devId + "}"))
            .andExpect(status().isOk());
        long taskId = parse(mvc.perform(postAs("/api/tasks", mgrToken,
            "{\"projectId\":" + projectId + ",\"title\":\"T\",\"priority\":\"LOW\"}")).andReturn())
            .get("id").asLong();
        mvc.perform(postAs("/api/tasks/" + taskId + "/transition", mgrToken, "{\"status\":\"IN_PROGRESS\"}"));
        mvc.perform(postAs("/api/tasks/" + taskId + "/transition", mgrToken, "{\"status\":\"IN_REVIEW\"}"));
        mvc.perform(postAs("/api/tasks/" + taskId + "/transition", mgrToken, "{\"status\":\"DONE\"}"));

        String devToken = login("tdev5", "secret123");
        mvc.perform(postAs("/api/tasks/" + taskId + "/transition", devToken, "{\"status\":\"CLOSED\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void nonMemberCannotSeeTask() throws Exception {
        long mgrId = register("tmgr6", "secret123");
        setRoleDirectly(mgrId, Role.MANAGER);
        String mgrToken = login("tmgr6", "secret123");
        long projectId = createProjectAs(mgrToken, "P");
        long taskId = parse(mvc.perform(postAs("/api/tasks", mgrToken,
            "{\"projectId\":" + projectId + ",\"title\":\"T\",\"priority\":\"LOW\"}")).andReturn())
            .get("id").asLong();

        register("outsider6", "secret123");
        String outToken = login("outsider6", "secret123");
        mvc.perform(getAs("/api/tasks/" + taskId, outToken)).andExpect(status().isNotFound());
    }

    @Test
    void taskListIsScopedToMembership() throws Exception {
        long mgrId = register("tmgr7", "secret123");
        setRoleDirectly(mgrId, Role.MANAGER);
        String mgrToken = login("tmgr7", "secret123");
        long projectId = createProjectAs(mgrToken, "P");
        mvc.perform(postAs("/api/tasks", mgrToken,
            "{\"projectId\":" + projectId + ",\"title\":\"T\",\"priority\":\"LOW\"}"));

        register("outsider7", "secret123");
        String outToken = login("outsider7", "secret123");
        mvc.perform(getAs("/api/tasks", outToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void assigneeMustBeProjectMember() throws Exception {
        long mgrId = register("tmgr8", "secret123");
        setRoleDirectly(mgrId, Role.MANAGER);
        String mgrToken = login("tmgr8", "secret123");
        long projectId = createProjectAs(mgrToken, "P");
        long outsiderId = register("notmember", "secret123");

        mvc.perform(postAs("/api/tasks", mgrToken,
            "{\"projectId\":" + projectId + ",\"title\":\"T\",\"priority\":\"LOW\",\"assigneeId\":" + outsiderId + "}"))
            .andExpect(status().isBadRequest());
    }
}
