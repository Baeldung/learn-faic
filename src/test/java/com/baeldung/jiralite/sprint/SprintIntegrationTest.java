package com.baeldung.jiralite.sprint;

import com.baeldung.jiralite.IntegrationTestBase;
import com.baeldung.jiralite.user.Role;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SprintIntegrationTest extends IntegrationTestBase {

    private long createProjectAs(String token) throws Exception {
        return parse(mvc.perform(postAs("/api/projects", token, "{\"name\":\"P\"}")).andReturn()).get("id").asLong();
    }

    @Test
    void managerCanCreateAndStartAndCompleteSprint() throws Exception {
        long mgrId = register("smgr1", "secret123");
        setRoleDirectly(mgrId, Role.MANAGER);
        String token = login("smgr1", "secret123");
        long projectId = createProjectAs(token);

        long sprintId = parse(mvc.perform(postAs("/api/sprints", token,
                        "{\"projectId\":" + projectId + ",\"name\":\"S1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PLANNED"))
                .andReturn()).get("id").asLong();

        mvc.perform(postAs("/api/sprints/" + sprintId + "/start", token, ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mvc.perform(postAs("/api/sprints/" + sprintId + "/complete", token, ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void cannotCompleteUnstartedSprint() throws Exception {
        long mgrId = register("smgr2", "secret123");
        setRoleDirectly(mgrId, Role.MANAGER);
        String token = login("smgr2", "secret123");
        long projectId = createProjectAs(token);
        long sprintId = parse(mvc.perform(postAs("/api/sprints", token,
                        "{\"projectId\":" + projectId + ",\"name\":\"S1\"}")).andReturn()).get("id").asLong();

        mvc.perform(postAs("/api/sprints/" + sprintId + "/complete", token, ""))
                .andExpect(status().isConflict());
    }

    @Test
    void cannotStartTwice() throws Exception {
        long mgrId = register("smgr3", "secret123");
        setRoleDirectly(mgrId, Role.MANAGER);
        String token = login("smgr3", "secret123");
        long projectId = createProjectAs(token);
        long sprintId = parse(mvc.perform(postAs("/api/sprints", token,
                        "{\"projectId\":" + projectId + ",\"name\":\"S1\"}")).andReturn()).get("id").asLong();

        mvc.perform(postAs("/api/sprints/" + sprintId + "/start", token, "")).andExpect(status().isOk());
        mvc.perform(postAs("/api/sprints/" + sprintId + "/start", token, "")).andExpect(status().isConflict());
    }

    @Test
    void developerCannotCreateSprint() throws Exception {
        long mgrId = register("smgr4", "secret123");
        setRoleDirectly(mgrId, Role.MANAGER);
        String mgrToken = login("smgr4", "secret123");
        long projectId = createProjectAs(mgrToken);

        long devId = register("sdev4", "secret123");
        mvc.perform(postAs("/api/projects/" + projectId + "/members", mgrToken, "{\"userId\":" + devId + "}"));
        String devToken = login("sdev4", "secret123");

        mvc.perform(postAs("/api/sprints", devToken, "{\"projectId\":" + projectId + ",\"name\":\"S\"}"))
                .andExpect(status().isForbidden());
    }
}
