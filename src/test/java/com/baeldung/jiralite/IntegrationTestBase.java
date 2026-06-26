package com.baeldung.jiralite;

import com.baeldung.jiralite.user.Role;
import com.baeldung.jiralite.user.User;
import com.baeldung.jiralite.user.UserRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class IntegrationTestBase {

    @Autowired
    protected WebApplicationContext context;
    @Autowired
    protected ObjectMapper json;
    @Autowired
    protected UserRepository userRepository;

    protected MockMvc mvc;

    @BeforeEach
    void setUpMvc() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    protected long register(String username, String password) throws Exception {
        String body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        MvcResult res = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn();
        if (res.getResponse().getStatus() != 201) {
            throw new IllegalStateException("register failed: " + res.getResponse().getStatus()
                    + " " + res.getResponse().getContentAsString());
        }
        JsonNode node = json.readTree(res.getResponse().getContentAsString());
        return node.get("id").asLong();
    }

    protected String login(String username, String password) throws Exception {
        String body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        MvcResult res = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn();
        if (res.getResponse().getStatus() != 200) {
            throw new IllegalStateException("login failed: " + res.getResponse().getStatus());
        }
        return json.readTree(res.getResponse().getContentAsString()).get("token").asText();
    }

    protected void setRoleDirectly(long userId, Role role) {
        User u = userRepository.findById(userId).orElseThrow();
        u.setRole(role);
        userRepository.save(u);
    }

    protected MockHttpServletRequestBuilder authed(MockHttpServletRequestBuilder b, String token) {
        return b.header("Authorization", "Bearer " + token);
    }

    protected MockHttpServletRequestBuilder getAs(String path, String token) {
        return authed(get(path), token);
    }

    protected MockHttpServletRequestBuilder postAs(String path, String token, String body) {
        return authed(post(path).contentType(MediaType.APPLICATION_JSON).content(body), token);
    }

    protected MockHttpServletRequestBuilder putAs(String path, String token, String body) {
        return authed(put(path).contentType(MediaType.APPLICATION_JSON).content(body), token);
    }

    protected MockHttpServletRequestBuilder patchAs(String path, String token, String body) {
        return authed(patch(path).contentType(MediaType.APPLICATION_JSON).content(body), token);
    }

    protected MockHttpServletRequestBuilder deleteAs(String path, String token) {
        return authed(delete(path), token);
    }

    protected JsonNode parse(MvcResult res) throws Exception {
        return json.readTree(res.getResponse().getContentAsString());
    }
}
