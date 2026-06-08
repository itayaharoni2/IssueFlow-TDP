package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.BaseIntegrationTest;
import com.att.tdp.issueflow.entity.Project;
import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.entity.enums.AuditAction;
import com.att.tdp.issueflow.entity.enums.TicketPriority;
import com.att.tdp.issueflow.entity.enums.TicketStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuditLogControllerTest extends BaseIntegrationTest {

    @Test
    void ticketCreate_writesAuditLog() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");

        // Create project via API so audit log is written
        String projectResponse = mockMvc.perform(post("/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "P1",
                                "ownerId", admin.getId()
                        ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long projectId = objectMapper.readTree(projectResponse).get("id").asLong();

        mockMvc.perform(post("/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", "Audited ticket",
                                "priority", "LOW",
                                "type", "BUG",
                                "projectId", projectId
                        ))))
                .andExpect(status().isOk());

        assertThat(auditLogRepository.findAll().stream()
                .anyMatch(l -> AuditAction.CREATE.equals(l.getAction())
                        && "Ticket".equals(l.getEntityType())))
                .isTrue();
    }

    @Test
    void ticketUpdate_writesAuditLog() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        mockMvc.perform(patch("/tickets/" + ticket.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "Updated"))))
                .andExpect(status().isOk());

        assertThat(auditLogRepository.findAll().stream()
                .anyMatch(l -> AuditAction.UPDATE.equals(l.getAction())
                        && "Ticket".equals(l.getEntityType())))
                .isTrue();
    }

    @Test
    void ticketDelete_writesAuditLog() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        mockMvc.perform(delete("/tickets/" + ticket.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        assertThat(auditLogRepository.findAll().stream()
                .anyMatch(l -> AuditAction.DELETE.equals(l.getAction())
                        && "Ticket".equals(l.getEntityType())))
                .isTrue();
    }

    @Test
    void autoAssignment_writesSystemAuditLog() throws Exception {
        User admin = createAdmin();
        createDeveloper("dev1");
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");

        // Creating a ticket without assigneeId should trigger auto-assignment
        mockMvc.perform(post("/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", "Auto-assigned",
                                "priority", "LOW",
                                "type", "BUG",
                                "projectId", project.getId()
                        ))))
                .andExpect(status().isOk());

        assertThat(auditLogRepository.findAll().stream()
                .anyMatch(l -> AuditAction.AUTO_ASSIGN.equals(l.getAction())
                        && "SYSTEM".equals(l.getActor())))
                .isTrue();
    }

    @Test
    void getAuditLogs_returnsAll() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");

        // Create project via API (generates audit log)
        mockMvc.perform(post("/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "P1",
                                "ownerId", admin.getId()
                        ))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/audit-logs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))));
    }

    @Test
    void getAuditLogs_filterByEntityType() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        createTicket(project, admin, null);

        mockMvc.perform(get("/audit-logs?entityType=Ticket")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].entityType", everyItem(is("Ticket"))));
    }

    @Test
    void getAuditLogs_filterByActor() throws Exception {
        User admin = createAdmin();
        createDeveloper("dev1");
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        // Auto-assign fires SYSTEM actor
        createTicket(project, admin, null);

        mockMvc.perform(get("/audit-logs?actor=SYSTEM")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].actor", everyItem(is("SYSTEM"))));
    }
}
