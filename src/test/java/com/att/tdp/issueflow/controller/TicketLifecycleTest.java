package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.BaseIntegrationTest;
import com.att.tdp.issueflow.entity.Project;
import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.entity.enums.TicketPriority;
import com.att.tdp.issueflow.entity.enums.TicketStatus;
import com.att.tdp.issueflow.entity.enums.TicketType;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TicketLifecycleTest extends BaseIntegrationTest {

    // ─── POST /tickets ────────────────────────────────────────────────────────

    @Test
    void createTicket_success() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");

        mockMvc.perform(post("/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", "Fix bug",
                                "priority", "HIGH",
                                "type", "BUG",
                                "projectId", project.getId()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("Fix bug")))
                .andExpect(jsonPath("$.status", is("TODO")))
                .andExpect(jsonPath("$.priority", is("HIGH")))
                .andExpect(jsonPath("$.projectId", is(project.getId().intValue())));
    }

    @Test
    void createTicket_nonExistingProject_returns404() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(post("/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", "Bug",
                                "priority", "LOW",
                                "type", "BUG",
                                "projectId", 99999
                        ))))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTicket_nonExistingAssignee_returns404() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");

        mockMvc.perform(post("/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", "Bug",
                                "priority", "LOW",
                                "type", "BUG",
                                "projectId", project.getId(),
                                "assigneeId", 99999
                        ))))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTicket_invalidPriority_returns400() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");

        String body = "{\"title\":\"Bug\",\"priority\":\"INVALID\",\"type\":\"BUG\",\"projectId\":" + project.getId() + "}";
        mockMvc.perform(post("/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /tickets ─────────────────────────────────────────────────────────

    @Test
    void getTicketsByProject_returnsOnlyThatProject() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project p1 = createProject(admin, "P1");
        Project p2 = createProject(admin, "P2");

        createTicket(p1, admin, null);
        createTicket(p2, admin, null);

        mockMvc.perform(get("/tickets/project/" + p1.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].projectId", is(p1.getId().intValue())));
    }

    @Test
    void getTicketById_success() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project p = createProject(admin, "P1");
        Ticket ticket = createTicket(p, admin, null);

        mockMvc.perform(get("/tickets/" + ticket.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(ticket.getId().intValue())));
    }

    // ─── Lifecycle transitions ────────────────────────────────────────────────

    @Test
    void transition_todoToInProgress_succeeds() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project p = createProject(admin, "P1");
        Ticket ticket = createTicket(p, admin, null, TicketStatus.TODO, TicketPriority.LOW);

        mockMvc.perform(patch("/tickets/" + ticket.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "IN_PROGRESS"))))
                .andExpect(status().isOk());
    }

    @Test
    void transition_inProgressToInReview_succeeds() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project p = createProject(admin, "P1");
        Ticket ticket = createTicket(p, admin, null, TicketStatus.IN_PROGRESS, TicketPriority.LOW);

        mockMvc.perform(patch("/tickets/" + ticket.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "IN_REVIEW"))))
                .andExpect(status().isOk());
    }

    @Test
    void transition_inReviewToDone_succeeds() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project p = createProject(admin, "P1");
        Ticket ticket = createTicket(p, admin, null, TicketStatus.IN_REVIEW, TicketPriority.LOW);

        mockMvc.perform(patch("/tickets/" + ticket.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "DONE"))))
                .andExpect(status().isOk());
    }

    @Test
    void transition_backward_inProgressToTodo_fails() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project p = createProject(admin, "P1");
        Ticket ticket = createTicket(p, admin, null, TicketStatus.IN_PROGRESS, TicketPriority.LOW);

        mockMvc.perform(patch("/tickets/" + ticket.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "TODO"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transition_skip_todoToDone_fails() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project p = createProject(admin, "P1");
        Ticket ticket = createTicket(p, admin, null, TicketStatus.TODO, TicketPriority.LOW);

        mockMvc.perform(patch("/tickets/" + ticket.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "DONE"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateDoneTicket_title_fails() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project p = createProject(admin, "P1");
        Ticket ticket = createTicket(p, admin, null, TicketStatus.DONE, TicketPriority.LOW);

        mockMvc.perform(patch("/tickets/" + ticket.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "New Title"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateDoneTicket_priority_fails() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project p = createProject(admin, "P1");
        Ticket ticket = createTicket(p, admin, null, TicketStatus.DONE, TicketPriority.LOW);

        mockMvc.perform(patch("/tickets/" + ticket.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("priority", "HIGH"))))
                .andExpect(status().isBadRequest());
    }

    // ─── Soft-delete / restore ────────────────────────────────────────────────

    @Test
    void softDeleteTicket_hidesFromNormalGet() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project p = createProject(admin, "P1");
        Ticket ticket = createTicket(p, admin, null);

        mockMvc.perform(delete("/tickets/" + ticket.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/tickets/" + ticket.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/tickets/project/" + p.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void getDeletedTickets_adminOnly() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project p = createProject(admin, "P1");
        Ticket ticket = createTicket(p, admin, null);
        ticket.setDeletedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        mockMvc.perform(get("/tickets/deleted?projectId=" + p.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void getDeletedTickets_developerForbidden() throws Exception {
        User admin = createAdmin();
        Project p = createProject(admin, "P1");
        String devToken = loginAsDeveloper("dev1");

        mockMvc.perform(get("/tickets/deleted?projectId=" + p.getId())
                        .header("Authorization", "Bearer " + devToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void restoreTicket_adminCanRestore() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project p = createProject(admin, "P1");
        Ticket ticket = createTicket(p, admin, null);
        ticket.setDeletedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        mockMvc.perform(post("/tickets/" + ticket.getId() + "/restore")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/tickets/" + ticket.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void restoreTicket_developerForbidden() throws Exception {
        User admin = createAdmin();
        Project p = createProject(admin, "P1");
        Ticket ticket = createTicket(p, admin, null);
        ticket.setDeletedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        String devToken = loginAsDeveloper("dev1");
        mockMvc.perform(post("/tickets/" + ticket.getId() + "/restore")
                        .header("Authorization", "Bearer " + devToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteTicket_notFound_returns404() throws Exception {
        String token = loginAsAdmin();
        mockMvc.perform(delete("/tickets/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
