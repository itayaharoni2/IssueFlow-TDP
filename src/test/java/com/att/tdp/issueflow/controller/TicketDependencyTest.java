package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.BaseIntegrationTest;
import com.att.tdp.issueflow.entity.Project;
import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.entity.TicketDependency;
import com.att.tdp.issueflow.entity.enums.TicketPriority;
import com.att.tdp.issueflow.entity.enums.TicketStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TicketDependencyTest extends BaseIntegrationTest {

    private String adminToken;
    private Project project;
    private User admin;

    private void setup() throws Exception {
        admin = createAdmin();
        adminToken = loginAndGetToken("admin", "secret");
        project = createProject(admin, "P1");
    }

    @Test
    void addDependency_success() throws Exception {
        setup();
        Ticket t1 = createTicket(project, admin, null);
        Ticket t2 = createTicket(project, admin, null);

        mockMvc.perform(post("/tickets/" + t1.getId() + "/dependencies")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("blockedBy", t2.getId()))))
                .andExpect(status().isOk());
    }

    @Test
    void getDependencies_returnsList() throws Exception {
        setup();
        Ticket t1 = createTicket(project, admin, null);
        Ticket blocker = createTicket(project, admin, null);

        TicketDependency dep = new TicketDependency();
        dep.setTicket(t1);
        dep.setBlockedBy(blocker);
        ticketDependencyRepository.save(dep);

        mockMvc.perform(get("/tickets/" + t1.getId() + "/dependencies")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(blocker.getId().intValue())))
                .andExpect(jsonPath("$[0].status", is("TODO")));
    }

    @Test
    void removeDependency_success() throws Exception {
        setup();
        Ticket t1 = createTicket(project, admin, null);
        Ticket blocker = createTicket(project, admin, null);

        TicketDependency dep = new TicketDependency();
        dep.setTicket(t1);
        dep.setBlockedBy(blocker);
        ticketDependencyRepository.save(dep);

        mockMvc.perform(delete("/tickets/" + t1.getId() + "/dependencies/" + blocker.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/tickets/" + t1.getId() + "/dependencies")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void addDependency_selfReference_returns400() throws Exception {
        setup();
        Ticket t1 = createTicket(project, admin, null);

        mockMvc.perform(post("/tickets/" + t1.getId() + "/dependencies")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("blockedBy", t1.getId()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addDependency_differentProject_returns400() throws Exception {
        setup();
        Ticket t1 = createTicket(project, admin, null);

        Project p2 = createProject(admin, "P2");
        Ticket t2 = createTicket(p2, admin, null);

        mockMvc.perform(post("/tickets/" + t1.getId() + "/dependencies")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("blockedBy", t2.getId()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addDependency_cyclePrevented() throws Exception {
        setup();
        Ticket t1 = createTicket(project, admin, null);
        Ticket t2 = createTicket(project, admin, null);

        // t1 blocked by t2
        TicketDependency dep = new TicketDependency();
        dep.setTicket(t1);
        dep.setBlockedBy(t2);
        ticketDependencyRepository.save(dep);

        // Now try t2 blocked by t1 — should be rejected
        mockMvc.perform(post("/tickets/" + t2.getId() + "/dependencies")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("blockedBy", t1.getId()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transitionToDone_withUnresolvedBlocker_fails() throws Exception {
        setup();
        Ticket t1 = createTicket(project, admin, null, TicketStatus.IN_REVIEW, TicketPriority.LOW);
        Ticket blocker = createTicket(project, admin, null, TicketStatus.TODO, TicketPriority.LOW);

        TicketDependency dep = new TicketDependency();
        dep.setTicket(t1);
        dep.setBlockedBy(blocker);
        ticketDependencyRepository.save(dep);

        mockMvc.perform(patch("/tickets/" + t1.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "DONE"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transitionToDone_allBlockersDone_succeeds() throws Exception {
        setup();
        Ticket t1 = createTicket(project, admin, null, TicketStatus.IN_REVIEW, TicketPriority.LOW);
        Ticket blocker = createTicket(project, admin, null, TicketStatus.DONE, TicketPriority.LOW);

        TicketDependency dep = new TicketDependency();
        dep.setTicket(t1);
        dep.setBlockedBy(blocker);
        ticketDependencyRepository.save(dep);

        mockMvc.perform(patch("/tickets/" + t1.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "DONE"))))
                .andExpect(status().isOk());
    }
}
