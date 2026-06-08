package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.BaseIntegrationTest;
import com.att.tdp.issueflow.entity.Project;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.entity.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProjectControllerTest extends BaseIntegrationTest {

    // ─── POST /projects ───────────────────────────────────────────────────────

    @Test
    void createProject_success() throws Exception {
        User owner = createAdmin();
        String token = loginAndGetToken("admin", "secret");

        mockMvc.perform(post("/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "MyProject",
                                "description", "A great project",
                                "ownerId", owner.getId()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("MyProject")))
                .andExpect(jsonPath("$.ownerId", is(owner.getId().intValue())));
    }

    @Test
    void createProject_nonExistingOwner_returns404() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(post("/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Project",
                                "ownerId", 99999
                        ))))
                .andExpect(status().isNotFound());
    }

    // ─── GET /projects ────────────────────────────────────────────────────────

    @Test
    void getProjects_returnsOnlyNonDeleted() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");

        Project active = createProject(admin, "Active");
        Project deleted = createProject(admin, "Deleted");
        deleted.setDeletedAt(LocalDateTime.now());
        projectRepository.save(deleted);

        mockMvc.perform(get("/projects")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("Active")))
                .andExpect(jsonPath("$[*].name", not(hasItem("Deleted"))));
    }

    @Test
    void getProjectById_success() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "MyProject");

        mockMvc.perform(get("/projects/" + project.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("MyProject")));
    }

    @Test
    void getProjectById_notFound_returns404() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(get("/projects/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ─── PATCH /projects/{id} ─────────────────────────────────────────────────

    @Test
    void updateProject_success() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "OldName");

        mockMvc.perform(patch("/projects/" + project.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "NewName"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/projects/" + project.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.name", is("NewName")));
    }

    // ─── DELETE /projects/{id} ────────────────────────────────────────────────

    @Test
    void softDeleteProject_success() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "ToDelete");

        mockMvc.perform(delete("/projects/" + project.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Should no longer appear in regular list
        mockMvc.perform(get("/projects")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[*].name", not(hasItem("ToDelete"))));
    }

    @Test
    void softDeleteProject_notFound_returns404() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(delete("/projects/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ─── GET /projects/deleted — ADMIN only ──────────────────────────────────

    @Test
    void getDeletedProjects_adminCanAccess() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");

        Project p = createProject(admin, "Ghost");
        p.setDeletedAt(LocalDateTime.now());
        projectRepository.save(p);

        mockMvc.perform(get("/projects/deleted")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("Ghost")));
    }

    @Test
    void getDeletedProjects_developerForbidden() throws Exception {
        String token = loginAsDeveloper("dev1");

        mockMvc.perform(get("/projects/deleted")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ─── POST /projects/{id}/restore — ADMIN only ────────────────────────────

    @Test
    void restoreProject_adminCanRestore() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");

        Project p = createProject(admin, "Restored");
        p.setDeletedAt(LocalDateTime.now());
        projectRepository.save(p);

        mockMvc.perform(post("/projects/" + p.getId() + "/restore")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/projects")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[*].name", hasItem("Restored")));
    }

    @Test
    void restoreProject_developerForbidden() throws Exception {
        User admin = createAdmin();
        Project p = createProject(admin, "Ghost");
        p.setDeletedAt(LocalDateTime.now());
        projectRepository.save(p);

        String devToken = loginAsDeveloper("dev1");

        mockMvc.perform(post("/projects/" + p.getId() + "/restore")
                        .header("Authorization", "Bearer " + devToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void restoreProject_notFound_returns404() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(post("/projects/99999/restore")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ─── GET /projects/{id}/workload ─────────────────────────────────────────

    @Test
    void getWorkload_returnsDevList() throws Exception {
        User admin = createAdmin();
        User dev = createDeveloper("dev1");
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "MyProject");

        createTicket(project, admin, dev);

        mockMvc.perform(get("/projects/" + project.getId() + "/workload")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId", is(dev.getId().intValue())))
                .andExpect(jsonPath("$[0].username", is("dev1")))
                .andExpect(jsonPath("$[0].openTicketCount", is(1)));
    }
}
