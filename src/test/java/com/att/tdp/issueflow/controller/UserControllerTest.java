package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.BaseIntegrationTest;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.entity.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest extends BaseIntegrationTest {

    // ─── POST /users ──────────────────────────────────────────────────────────

    @Test
    void createUser_success() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", "alice",
                                "email", "alice@example.com",
                                "fullName", "Alice Smith",
                                "role", "DEVELOPER"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username", is("alice")))
                .andExpect(jsonPath("$.email", is("alice@example.com")))
                .andExpect(jsonPath("$.fullName", is("Alice Smith")))
                .andExpect(jsonPath("$.role", is("DEVELOPER")))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void createUser_passwordNotReturned() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", "bob",
                                "email", "bob@example.com",
                                "fullName", "Bob Jones",
                                "role", "ADMIN",
                                "password", "mypassword"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void createUser_duplicateUsername_fails() throws Exception {
        createUser("alice", "alice@example.com", Role.DEVELOPER);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", "alice",
                                "email", "alice2@example.com",
                                "fullName", "Alice 2",
                                "role", "DEVELOPER"
                        ))))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void createUser_duplicateEmail_fails() throws Exception {
        createUser("alice", "alice@example.com", Role.DEVELOPER);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", "alice2",
                                "email", "alice@example.com",
                                "fullName", "Alice 2",
                                "role", "DEVELOPER"
                        ))))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void createUser_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", "alice",
                                "email", "not-an-email",
                                "fullName", "Alice",
                                "role", "DEVELOPER"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_missingUsername_returns400() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "alice@example.com",
                                "fullName", "Alice",
                                "role", "DEVELOPER"
                        ))))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /users ───────────────────────────────────────────────────────────

    @Test
    void getAllUsers_requiresAuth() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllUsers_returnsListWithNoPassword() throws Exception {
        String token = loginAsAdmin();
        createDeveloper("dev1");

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void getUserById_returnsCorrectUser() throws Exception {
        User user = createDeveloper("dev1");
        String token = loginAsAdmin();

        mockMvc.perform(get("/users/" + user.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("dev1")))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getUserById_notFound_returns404() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(get("/users/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ─── POST /users/update/{id} ──────────────────────────────────────────────

    @Test
    void updateUser_success() throws Exception {
        User user = createDeveloper("dev1");
        String token = loginAsAdmin();

        mockMvc.perform(post("/users/update/" + user.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("fullName", "Updated Name"))))
                .andExpect(status().isOk());

        // Verify the update was persisted
        mockMvc.perform(get("/users/" + user.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.fullName", is("Updated Name")));
    }

    // ─── DELETE /users/{id} ───────────────────────────────────────────────────

    @Test
    void deleteUser_success() throws Exception {
        User user = createDeveloper("dev1");
        String token = loginAsAdmin();

        mockMvc.perform(delete("/users/" + user.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/" + user.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_notFound_returns404() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(delete("/users/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
