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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CsvExportImportTest extends BaseIntegrationTest {

    // ─── CSV Export ───────────────────────────────────────────────────────────

    @Test
    void export_returnsCsvContentType() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        createTicket(project, admin, null);

        mockMvc.perform(get("/tickets/export?projectId=" + project.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(
                        result.getResponse().getContentType()).contains("text/csv"))
                .andExpect(header().string("Content-Disposition",
                        containsString("attachment")));
    }

    @Test
    void export_containsRequiredColumns() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        createTicket(project, admin, null);

        MvcResult result = mockMvc.perform(get("/tickets/export?projectId=" + project.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String csv = result.getResponse().getContentAsString();
        String header = csv.split("\n")[0];
        assertThat(header).contains("id", "title", "description", "status", "priority", "type", "assigneeId");
    }

    @Test
    void export_onlyIncludesTicketsForProject() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project p1 = createProject(admin, "P1");
        Project p2 = createProject(admin, "P2");

        Ticket t1 = createTicket(p1, admin, null);
        Ticket t2 = createTicket(p2, admin, null);

        MvcResult result = mockMvc.perform(get("/tickets/export?projectId=" + p1.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String csv = result.getResponse().getContentAsString();
        // Should contain p1 ticket id but not p2 ticket id
        assertThat(csv).contains(t1.getId().toString());
        assertThat(csv).doesNotContain(t2.getId().toString());
    }

    @Test
    void export_excludesSoftDeletedTickets() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");

        Ticket active = createTicket(project, admin, null);
        Ticket deleted = createTicket(project, admin, null);
        deleted.setDeletedAt(java.time.LocalDateTime.now());
        ticketRepository.save(deleted);

        MvcResult result = mockMvc.perform(get("/tickets/export?projectId=" + project.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String csv = result.getResponse().getContentAsString();
        assertThat(csv).contains(active.getId().toString());
        assertThat(csv).doesNotContain(deleted.getId().toString());
    }

    // ─── CSV Import ───────────────────────────────────────────────────────────

    @Test
    void import_validCsv_createsTickets() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");

        String csvContent = "title,description,status,priority,type,assigneeId\n" +
                "Ticket One,First desc,TODO,LOW,BUG,\n" +
                "Ticket Two,Second desc,TODO,HIGH,FEATURE,\n";

        MockMultipartFile file = new MockMultipartFile("file", "tickets.csv",
                "text/csv", csvContent.getBytes());

        mockMvc.perform(multipart("/tickets/import")
                        .file(file)
                        .param("projectId", project.getId().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created", is(2)))
                .andExpect(jsonPath("$.failed", is(0)));
    }

    @Test
    void import_partialSuccess_reportsFailedRows() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");

        String csvContent = "title,description,status,priority,type,assigneeId\n" +
                "Valid Ticket,desc,TODO,LOW,BUG,\n" +
                "Invalid Ticket,desc,TODO,BAD_PRIORITY,BUG,\n"; // invalid priority

        MockMultipartFile file = new MockMultipartFile("file", "tickets.csv",
                "text/csv", csvContent.getBytes());

        mockMvc.perform(multipart("/tickets/import")
                        .file(file)
                        .param("projectId", project.getId().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created", is(1)))
                .andExpect(jsonPath("$.failed", is(1)))
                .andExpect(jsonPath("$.errors", hasSize(1)));
    }

    @Test
    void import_nonExistingProject_returns404() throws Exception {
        String token = loginAsAdmin();

        String csvContent = "title,description,status,priority,type,assigneeId\nTicket,desc,TODO,LOW,BUG,\n";
        MockMultipartFile file = new MockMultipartFile("file", "tickets.csv",
                "text/csv", csvContent.getBytes());

        mockMvc.perform(multipart("/tickets/import")
                        .file(file)
                        .param("projectId", "99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void import_handlesQuotedFieldsWithCommas() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");

        // Description contains a comma, properly quoted
        String csvContent = "title,description,status,priority,type,assigneeId\n" +
                "\"Ticket, One\",\"First, second\",TODO,LOW,BUG,\n";

        MockMultipartFile file = new MockMultipartFile("file", "tickets.csv",
                "text/csv", csvContent.getBytes());

        mockMvc.perform(multipart("/tickets/import")
                        .file(file)
                        .param("projectId", project.getId().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created", is(1)))
                .andExpect(jsonPath("$.failed", is(0)));
    }

    @Test
    void import_csvWithMissingRequiredColumn_returns200WithFailures() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");

        // Missing the "status" column completely
        String csvContent = "title,description,priority,type,assigneeId\n" +
                "Ticket One,First desc,LOW,BUG,\n";

        MockMultipartFile file = new MockMultipartFile("file", "tickets.csv",
                "text/csv", csvContent.getBytes());

        mockMvc.perform(multipart("/tickets/import")
                        .file(file)
                        .param("projectId", project.getId().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created", is(0)))
                .andExpect(jsonPath("$.failed", is(1)));
    }

    @Test
    void import_csvWithExtraColumn_ignoredSafely() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");

        // Has an unexpected extra column
        String csvContent = "title,description,status,priority,type,assigneeId,extraColumn\n" +
                "Ticket One,First desc,TODO,LOW,BUG,,superfluousData\n";

        MockMultipartFile file = new MockMultipartFile("file", "tickets.csv",
                "text/csv", csvContent.getBytes());

        mockMvc.perform(multipart("/tickets/import")
                        .file(file)
                        .param("projectId", project.getId().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created", is(1)))
                .andExpect(jsonPath("$.failed", is(0)));
    }

    @Test
    void import_csvWithEmbeddedNewlinesInQuotes_handlesCorrectly() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");

        // Description spans multiple lines enclosed in quotes
        String csvContent = "title,description,status,priority,type,assigneeId\n" +
                "Ticket One,\"First line\nSecond line\",TODO,LOW,BUG,\n";

        MockMultipartFile file = new MockMultipartFile("file", "tickets.csv",
                "text/csv", csvContent.getBytes());

        mockMvc.perform(multipart("/tickets/import")
                        .file(file)
                        .param("projectId", project.getId().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created", is(1)))
                .andExpect(jsonPath("$.failed", is(0)));
    }
}
