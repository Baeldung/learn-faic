package com.baeldung.jiralite.comment;

import com.baeldung.jiralite.IntegrationTestBase;
import com.baeldung.jiralite.user.Role;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommentIntegrationTest extends IntegrationTestBase {

    @Test
    void viewerCanCommentOnTaskInProject() throws Exception {
        long mgrId = register("cmgr", "secret123");
        setRoleDirectly(mgrId, Role.MANAGER);
        String mgrToken = login("cmgr", "secret123");
        long projectId = parse(mvc.perform(postAs("/api/projects", mgrToken, "{\"name\":\"P\"}")).andReturn())
                .get("id").asLong();
        long viewerId = register("cviewer", "secret123");
        setRoleDirectly(viewerId, Role.VIEWER);
        mvc.perform(postAs("/api/projects/" + projectId + "/members", mgrToken, "{\"userId\":" + viewerId + "}"));
        long taskId = parse(mvc.perform(postAs("/api/tasks", mgrToken,
                        "{\"projectId\":" + projectId + ",\"title\":\"T\",\"priority\":\"LOW\"}")).andReturn())
                .get("id").asLong();
        String viewerToken = login("cviewer", "secret123");

        mvc.perform(postAs("/api/tasks/" + taskId + "/comments", viewerToken, "{\"body\":\"hello\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.body").value("hello"));

        mvc.perform(getAs("/api/tasks/" + taskId + "/comments", viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void nonMemberCannotComment() throws Exception {
        long mgrId = register("cmgr2", "secret123");
        setRoleDirectly(mgrId, Role.MANAGER);
        String mgrToken = login("cmgr2", "secret123");
        long projectId = parse(mvc.perform(postAs("/api/projects", mgrToken, "{\"name\":\"P\"}")).andReturn())
                .get("id").asLong();
        long taskId = parse(mvc.perform(postAs("/api/tasks", mgrToken,
                        "{\"projectId\":" + projectId + ",\"title\":\"T\",\"priority\":\"LOW\"}")).andReturn())
                .get("id").asLong();

        register("outc", "secret123");
        String outToken = login("outc", "secret123");
        mvc.perform(postAs("/api/tasks/" + taskId + "/comments", outToken, "{\"body\":\"hi\"}"))
                .andExpect(status().isNotFound());
    }
}
