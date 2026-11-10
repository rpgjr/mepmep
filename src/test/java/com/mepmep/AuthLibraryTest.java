package com.mepmep;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:test;MODE=MySQL",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AuthLibraryTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private String register(String email) throws Exception {
        String body = """
            {"email":"%s","displayName":"Tester","password":"secret123"}""".formatted(email);
        String res = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return json.readTree(res).get("token").asText();
    }

    @Test
    void registerLoginAndRejectDuplicate() throws Exception {
        register("a@test.com");
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"a@test.com\",\"displayName\":\"x\",\"password\":\"secret123\"}"))
            .andExpect(status().isConflict());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"a@test.com\",\"password\":\"secret123\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.token").exists());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"a@test.com\",\"password\":\"wrong\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void corsAllowsConfiguredOriginOnly() throws Exception {
        mvc.perform(options("/api/auth/login")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type,authorization"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mvc.perform(get("/api/auth/me").header("Origin", "https://evil.example"))
            .andExpect(status().isForbidden());
    }

    @Test
    void healthEndpointIsPublic() throws Exception {
        mvc.perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void libraryNeedsLogin() throws Exception {
        mvc.perform(get("/api/library/favorites")).andExpect(status().isUnauthorized());
    }

    @Test
    void favoritesAndProgressFlow() throws Exception {
        String auth = "Bearer " + register("b@test.com");
        String item = "{\"mediaType\":\"movie\",\"tmdbId\":603,\"title\":\"The Matrix\",\"posterPath\":\"/p.jpg\"}";

        mvc.perform(post("/api/library/favorites").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content(item))
            .andExpect(status().isCreated());
        mvc.perform(post("/api/library/favorites").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content(item))
            .andExpect(status().isConflict());
        mvc.perform(get("/api/library/favorites").header("Authorization", auth))
            .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/library/status/movie/603").header("Authorization", auth))
            .andExpect(jsonPath("$.favorite").value(true)).andExpect(jsonPath("$.watchlist").value(false));
        mvc.perform(delete("/api/library/favorites/movie/603").header("Authorization", auth))
            .andExpect(status().isNoContent());
        mvc.perform(get("/api/library/favorites").header("Authorization", auth))
            .andExpect(jsonPath("$", hasSize(0)));

        String prog = "{\"mediaType\":\"tv\",\"tmdbId\":1396,\"title\":\"Breaking Bad\",\"season\":2,\"episode\":3,\"positionSec\":120,\"durationSec\":2700}";
        mvc.perform(put("/api/history").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content(prog)).andExpect(status().isOk());
        mvc.perform(put("/api/history").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content(prog.replace("120", "300"))).andExpect(status().isOk());
        mvc.perform(get("/api/history").header("Authorization", auth))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].positionSec").value(300));
    }
}
