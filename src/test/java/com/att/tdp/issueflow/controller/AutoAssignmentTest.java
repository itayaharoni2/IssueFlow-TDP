package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.BaseIntegrationTest;
import com.att.tdp.issueflow.entity.Project;
import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.entity.enums.Role;
import com.att.tdp.issueflow.entity.enums.TicketPriority;
import com.att.tdp.issueflow.entity.enums.TicketStatus;
import com.att.tdp.issueflow.entity.enums.TicketType;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AutoAssignmentTest extends BaseIntegrationTest {

    @Test
    void createTicket_noAssigneeId_autoAssignsToDeveloper() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        User dev = createDeveloper("dev1");
        Project project = createProject(admin, "P1");

        mockMvc.perform(post("/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", "Auto-assigned ticket",
                                "priority", "LOW",
                                "type", "BUG",
                                "projectId", project.getId()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigneeId", is(dev.getId().intValue())));
    }

    @Test
    void createTicket_withExplicitAssignee_doesNotAutoAssign() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        User dev1 = createDeveloper("dev1");
        User dev2 = createDeveloper("dev2");
        Project project = createProject(admin, "P1");

        // Give dev1 more workload so auto-assign would pick dev2
        createTicket(project, admin, dev1);

        mockMvc.perform(post("/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", "Explicit ticket",
                                "priority", "LOW",
                                "type", "BUG",
                                "projectId", project.getId(),
                                "assigneeId", dev1.getId()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigneeId", is(dev1.getId().intValue())));
    }

    @Test
    void createTicket_noDevs_assigneeIsNull() throws Exception {
        // Only admin user, no developers
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");

        mockMvc.perform(post("/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", "No devs ticket",
                                "priority", "LOW",
                                "type", "BUG",
                                "projectId", project.getId()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigneeId").doesNotExist());
    }

    @Test
    void autoAssign_picksLeastLoadedDeveloper() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        User dev1 = createDeveloper("dev1");
        User dev2 = createDeveloper("dev2");
        Project project = createProject(admin, "P1");

        // dev1 gets 2 tickets, dev2 gets 0 → next auto-assign should go to dev2
        createTicket(project, admin, dev1, TicketStatus.TODO, TicketPriority.LOW);
        createTicket(project, admin, dev1, TicketStatus.TODO, TicketPriority.LOW);

        mockMvc.perform(post("/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", "New ticket",
                                "priority", "LOW",
                                "type", "BUG",
                                "projectId", project.getId()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigneeId", is(dev2.getId().intValue())));
    }

    @Test
    void autoAssign_doneTicketsNotCountedInWorkload() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        User dev1 = createDeveloper("dev1");
        User dev2 = createDeveloper("dev2");
        Project project = createProject(admin, "P1");

        // dev1 has 2 DONE tickets → workload=0; dev2 has 1 TODO ticket → workload=1
        // so auto-assign should pick dev1 (lower workload)
        createTicket(project, admin, dev1, TicketStatus.DONE, TicketPriority.LOW);
        createTicket(project, admin, dev1, TicketStatus.DONE, TicketPriority.LOW);
        createTicket(project, admin, dev2, TicketStatus.TODO, TicketPriority.LOW);

        mockMvc.perform(post("/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", "New ticket",
                                "priority", "LOW",
                                "type", "BUG",
                                "projectId", project.getId()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigneeId", is(dev1.getId().intValue())));
    }

    @Test
    void autoAssign_adminUsersNotSelected() throws Exception {
        // Only admin user exists → should result in null assignee
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");

        mockMvc.perform(post("/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", "Ticket",
                                "priority", "LOW",
                                "type", "BUG",
                                "projectId", project.getId()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigneeId").doesNotExist());
    }
}
