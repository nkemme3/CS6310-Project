package edu.gatech.cs6310.powergrid.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.EnumSet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * End-to-end auth sanity: /api/auth/login returns a bearer token, anonymous
 * writes are rejected with 401, a token from a VIEWER can read but not write,
 * and ADMIN can write.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private UserService users;
    @Autowired private ObjectMapper json;

    @BeforeEach
    void seed() {
        // AdminSeeder runs on ApplicationReadyEvent — if it didn't run in the test context, force it.
        if (users.findUser("admin").isEmpty()) {
            users.seedIfAbsent("admin", "admin123", EnumSet.of(Role.ADMIN));
        }
        if (users.findUser("viewer1").isEmpty()) {
            users.createUser("viewer1", "readonly", EnumSet.of(Role.VIEWER));
        }
    }

    private String login(String username, String password) throws Exception {
        String body = json.writeValueAsString(new AuthController.LoginRequest(username, password));
        String resp = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        JsonNode tree = json.readTree(resp);
        return tree.get("token").asText();
    }

    @Test
    void anonymousWriteIsRejected() throws Exception {
        mvc.perform(post("/api/companies").contentType(MediaType.APPLICATION_JSON)
                .content("{\"longName\":\"X\",\"shortName\":\"X\",\"standardRate\":0.1}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void adminCanCreateCompany() throws Exception {
        String token = login("admin", "admin123");
        mvc.perform(post("/api/companies")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"longName\":\"Test Co\",\"shortName\":\"TEST1\",\"standardRate\":0.1}"))
            .andExpect(status().isCreated());
    }

    @Test
    void viewerCanListButCannotWrite() throws Exception {
        String token = login("viewer1", "readonly");
        mvc.perform(get("/api/companies").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
        mvc.perform(post("/api/companies")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"longName\":\"Nope Co\",\"shortName\":\"NOPE\",\"standardRate\":0.1}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void badCredentialsReturn401() throws Exception {
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void meReturnsSessionInfoWhenAuthenticated() throws Exception {
        String token = login("admin", "admin123");
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("admin"))
            .andExpect(jsonPath("$.roles[0]").value("ADMIN"));
    }
}
